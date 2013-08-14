package info.mendlin;

import java.math.BigInteger;

import org.jllvm.LLVMAddInstruction;
import org.jllvm.LLVMBasicBlock;
import org.jllvm.LLVMConstantInteger;
import org.jllvm.LLVMFunction;
import org.jllvm.LLVMFunctionType;
import org.jllvm.LLVMInstructionBuilder;
import org.jllvm.LLVMIntegerType;
import org.jllvm.LLVMModule;
import org.jllvm.LLVMReturnInstruction;
import org.jllvm.LLVMType;
import org.jllvm.LLVMValue;
import org.jllvm.bindings.Analysis;
import org.jllvm.bindings.Core;
import org.jllvm.bindings.LLVMCallConv;
import org.jllvm.bindings.LLVMVerifierFailureAction;
import org.jllvm.bindings.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import org.jllvm.bindings.SWIGTYPE_p_LLVMOpaqueBuilder;
import org.jllvm.bindings.SWIGTYPE_p_LLVMOpaqueContext;
import org.jllvm.bindings.SWIGTYPE_p_LLVMOpaqueModule;
import org.jllvm.bindings.SWIGTYPE_p_LLVMOpaqueType;
import org.jllvm.bindings.SWIGTYPE_p_LLVMOpaqueValue;

/**
 *Migration of LLVM example, ModuleMaker.cpp
 *Create a simple module, which is equivalent to this C snippet: 
 * int fac()
 * {
 * 		return 2 + 4;
 * }
 * 
 *IR:
 *	; ModuleID = 'test'
 *	define i32 @fac() {
 *	entry:
 * 		ret i32 6
 *	}
 *
 * @author linmengl
 *	
 */
public class ModuleMaker {
	static {
		System.loadLibrary("jllvm");
	}
	
	public static void main(String[] args) {
		usePureCInterface();
		useJLLVMInterface();
	}
	
	/**
	 * Object-oriented interface by JLLVM
	 */
	private static void useJLLVMInterface() {
		// Context is useful for multi-thread compiling.
		// I can ignore context
		LLVMModule module = new LLVMModule("test");
		
		// Declare function, first declare function type.
		LLVMType params[] = {};		
		LLVMFunctionType functype = new LLVMFunctionType(LLVMIntegerType.i32, params, false);
		LLVMFunction func = new LLVMFunction(module, "fac", functype);
		
		// Position builder before use.
		LLVMBasicBlock bb = func.appendBasicBlock("entry");		
		LLVMInstructionBuilder builder = new LLVMInstructionBuilder();
		builder.positionBuilderAtEnd(bb);		
		
		LLVMValue two = LLVMConstantInteger.constantInteger(LLVMIntegerType.i32, 2, false);
		LLVMValue three = LLVMConstantInteger.constantInteger(LLVMIntegerType.i32, 4, false);
		// When creating a new instruction, it's appended by the builder, and returns a reference Value.
		LLVMAddInstruction add = new LLVMAddInstruction(builder, two , three, false, "add result");
		new LLVMReturnInstruction(builder, add);		
		
		Analysis.LLVMVerifyModule(module.getInstance(), LLVMVerifierFailureAction.LLVMPrintMessageAction, null);
		module.dump();
	}

	/**
	 * Create IR with pure C interface.
	 * The C interface is purely org.jllvm.bindings
	 */
	private static void usePureCInterface() {
		// Context and Module
		SWIGTYPE_p_LLVMOpaqueContext context = Core.LLVMContextCreate();
		SWIGTYPE_p_LLVMOpaqueModule module = Core.LLVMModuleCreateWithName("test");
		
		// Function declare
		SWIGTYPE_p_LLVMOpaqueType functype = Core.LLVMFunctionType(Core.LLVMInt32TypeInContext(context), null, 0, 0);
		SWIGTYPE_p_LLVMOpaqueValue fac = Core.LLVMAddFunction(module, "fac", functype);
		Core.LLVMSetFunctionCallConv(fac, LLVMCallConv.LLVMCCallConv.swigValue());
		
		// BasicBlock and a Builder, position Builder before use.
		SWIGTYPE_p_LLVMOpaqueBasicBlock bb = Core.LLVMAppendBasicBlock(fac, "entry");
		SWIGTYPE_p_LLVMOpaqueBuilder builder = Core.LLVMCreateBuilder();
		Core.LLVMPositionBuilderAtEnd(builder, bb);
		
		// Value is used in compile time, every node in AST is a Value. 
		SWIGTYPE_p_LLVMOpaqueValue two = Core.LLVMConstInt(Core.LLVMInt32Type(), BigInteger.valueOf(2), 1);
		SWIGTYPE_p_LLVMOpaqueValue three = Core.LLVMConstInt(Core.LLVMInt32Type(), BigInteger.valueOf(3), 1);
		SWIGTYPE_p_LLVMOpaqueValue add = Core.LLVMBuildAdd(builder, two, three, "add result");
		Core.LLVMBuildRet(builder, add);		
		Core.LLVMDumpModule(module);
	}
}
