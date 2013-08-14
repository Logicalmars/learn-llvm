package info.mendlin;

import org.jllvm.LLVMAddInstruction;
import org.jllvm.LLVMArgument;
import org.jllvm.LLVMBasicBlock;
import org.jllvm.LLVMBranchInstruction;
import org.jllvm.LLVMCallInstruction;
import org.jllvm.LLVMConstantInteger;
import org.jllvm.LLVMFunction;
import org.jllvm.LLVMFunctionType;
import org.jllvm.LLVMInstructionBuilder;
import org.jllvm.LLVMIntegerComparison;
import org.jllvm.LLVMIntegerType;
import org.jllvm.LLVMModule;
import org.jllvm.LLVMReturnInstruction;
import org.jllvm.LLVMSubtractInstruction;
import org.jllvm.LLVMType;
import org.jllvm.LLVMValue;
import org.jllvm.bindings.LLVMIntPredicate;

/**
 *Migrated LLVM examples. fibonacci.cpp
 *Involving creating a function. JIT part doesn't work currently.
 * @author linmengl
 *
 */
public class Fibonacci {
	static {
		System.loadLibrary("jllvm");		
	}
	
	public static void main(String[] args){
		
		LLVMModule mo = new LLVMModule("fibonacci_example");
		createFibFunction(mo);
		createShowFunction(mo);
		
		mo.verify();
		mo.dump();
		mo.writeBitcodeToFile("fib.bc");
	}

	/**
	 * int show_fib()
	 * {
	 * 		return fib(24);
	 * }
	 * 
	 * @param mo Module where the function get inserted.
	 */
	private static void createShowFunction(LLVMModule mo)
	{
//		define i32 @show_fib() {
//		entry:
//		  %call_fib = call i32 (i32, ...)* @fib(i32 24)
//		  ret i32 %call_fib
//		}
		LLVMType param[] = {};
		LLVMFunctionType functype = new LLVMFunctionType(LLVMIntegerType.i32, param, false);
		LLVMFunction func = new LLVMFunction(mo, "show_fib", functype);
		
		LLVMBasicBlock bb = func.appendBasicBlock("entry");
		LLVMInstructionBuilder builder = new LLVMInstructionBuilder();
		builder.positionBuilderAtEnd(bb);
		
		LLVMValue myarg = LLVMConstantInteger.constantInteger(LLVMIntegerType.i32, 24, false);
		
		LLVMCallInstruction call_fib = new LLVMCallInstruction(builder, mo.getFirstFunction(), new LLVMValue[] {myarg}, "call_fib");
		new LLVMReturnInstruction(builder, call_fib);
	}
	    
	/**
	 * int fib(int x)
	 * {	
	 * 		if (x<=2) 			
	 * 			return 1;
	 * 		else
	 * 			return fib(x-1) + fib(x-2);
	 * } 
	 * @param mo function insert position
	 */
	private static void createFibFunction(LLVMModule mo) {
//		define i32 @fib(i32 %AnArg, ...) {
//		entry:
//		  %cmp = icmp sle i32 %AnArg, 2
//		  br i1 %cmp, label %return, label %recurse
//
//		return:                                           ; preds = %entry
//		  ret i32 1
//
//		recurse:                                          ; preds = %entry
//		  %x_minus_one = sub i32 %AnArg, 1
//		  %fibx1 = tail call i32 (i32, ...)* @fib(i32 %x_minus_one)
//		  %x_minus_two = sub i32 %AnArg, 2
//		  %fibx2 = tail call i32 (i32, ...)* @fib(i32 %x_minus_two)
//		  %Sum = add i32 %fibx1, %fibx2
//		  ret i32 %Sum
//		}
		// Function declare, how to deal with parameters.
		LLVMType param[] = {LLVMIntegerType.i32}; 
		LLVMFunctionType functype = new LLVMFunctionType(LLVMIntegerType.i32, param, true);
		LLVMFunction func = new LLVMFunction(mo, "fib", functype);
		LLVMArgument argX = func.getFirstParameter();
		argX.setValueName("AnArg");
		
		LLVMValue two = LLVMConstantInteger.constantInteger(LLVMIntegerType.i32, 2, false);
		LLVMValue one = LLVMConstantInteger.constantInteger(LLVMIntegerType.i32, 1, false);
		
		LLVMBasicBlock entry = func.appendBasicBlock("entry");
		LLVMBasicBlock ret = func.appendBasicBlock("return");
		LLVMBasicBlock recurse = func.appendBasicBlock("recurse");		
		
		LLVMInstructionBuilder builder = new LLVMInstructionBuilder();
		
		// Build entry block
		builder.positionBuilderAtEnd(entry);		
		LLVMIntegerComparison cmp = new LLVMIntegerComparison(builder, LLVMIntPredicate.LLVMIntSLE, argX, two, "cmp");
		new LLVMBranchInstruction(builder, cmp, ret, recurse);
		
		// Build return block
		builder.positionBuilderAtEnd(ret);
		new LLVMReturnInstruction(builder, one);
		
		// Build recurse block
		builder.positionBuilderAtEnd(recurse);
		
		// Create fib(x-1)
		LLVMSubtractInstruction x_minus_one = new LLVMSubtractInstruction(builder, argX, one, false, "x_minus_one");
		LLVMCallInstruction call1 = new LLVMCallInstruction(builder, func, new LLVMValue[] {x_minus_one}, "fibx1");
		call1.setTailCall(true);
		
		// Create fib(x-2)
		LLVMSubtractInstruction x_minus_two = new LLVMSubtractInstruction(builder, argX, two, false, "x_minus_two");
		LLVMCallInstruction call2 = new LLVMCallInstruction(builder, func, new LLVMValue[] {x_minus_two}, "fibx2");
		call2.setTailCall(true);
		
		LLVMAddInstruction sum = new LLVMAddInstruction(builder, call1, call2, false, "Sum");
		new LLVMReturnInstruction(builder, sum);
	}
}
