class Axel {
	parentInheritedMethod(): Bool {
		false
	}

}

class Wheel inherits Axel {
	five: Int <- 5;
	truth: Bool <- false;
	zero: Int;	
	
	neg: Int <- -1;
	add: Int <- 1 + 2 - 5;
	mult: Int <- 4 * 3 / 2;
	paren: Int <- 1 + ((2 - 3) - 5) + 8;
	precedence: Int <- 1 + 4 * 2;
	
	positive : int <- 1;
  	negative : int <- -1;
  	z : int <- 0;
	
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
	
	callMethod() : Int {
		temp:Int <- 1;
		testThisToo:Bool <- true;
		{
			temp <- callMethod2();
			testThisToo <- false;
			temp;
		}
	}
	
	callMethod2() : Int {
		{ 
			9;
		}
	}
	

	sign(i : Int) : Int {
	  	if i < 0 then
	    	negative
	    else
	      	 if i > 0 then
        		positive
      		else
        		z
        	fi
	    fi
	}
	
	countdown(n : int) : int {
      i : int <- 0;
      {
        while n > 0
        loop
        {
          i <- i + n;
          n <- n - 1;
        }
        pool;
        i;
      }
    }

	
}