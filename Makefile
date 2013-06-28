CXX := clang++
IFLAG := -I/usr/include/i386-linux-gnu/c++/4.7/
CONFIG := `llvm-config --cppflags --ldflags --libs core jit native` -rdynamic
CONFIG1 := `llvm-config --cppflags --ldflags --libs core`
CONFIG2 := `llvm-config --cppflags --ldflags --libs jit interpreter nativecodegen`

handIR: manu/handIR.cpp 
	$(CXX) -O3 manu/handIR.cpp $(CONFIG2) $(IFLAG) -o handIR

toy:	
	$(CXX) -O3 toy.cpp $(CONFIG) $(IFLAG) -o toy 

parser: parser.cpp main.cpp tokens.cpp codegen.cpp
	$(CXX) main.cpp parser.cpp tokens.cpp codegen.cpp $(CONFIG1) $(IFLAG) -o parser

parser.cpp: parser.y
	bison -d -o $@ $^

parser.hpp: parser.cpp

tokens.cpp: tokens.l parser.hpp
	lex -o $@ $^

clean:	
	rm handIR toy parser.cpp parser.hpp parser tokens.cpp