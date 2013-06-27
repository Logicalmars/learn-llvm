#include <iostream>
#include "node.h"

extern NBlock* programBlock;
extern int yyparse();

int main()
{
	yyparse();
	std::cout << programBlock << std::endl;
	return 0;
}