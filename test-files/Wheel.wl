class Wheel {
	five: Int <- 5;
	truth: Bool <- false;
	zero: Int;	
	
	simpleBoolReturnFalse() : Bool {
		false
	}
	
	simpleBoolReturnTrue() : Bool {
		true
	}
	
	simpleIntReturnTest() : Int { 5 }
	
	localVar : Int <- 3; 
	
	simpleLocalVariableReturnTest() : Int { localVar }
	
	toBeAssigned : Int <- 99;
	localVarWithInnerAssignment(y: Int) : Int {
		{
			toBeAssigned <- y;
			toBeAssigned;
		}
	}
	
	innerMethodAssignment(y: Int) : Int {
		{
			y <- 6;
			y;
		}
	}
}