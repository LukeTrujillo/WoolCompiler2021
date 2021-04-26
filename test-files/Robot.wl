class Wheel {
	
	currentSpeed: Int;
	wheelDiameter: Int;

	init(startSpeed: Int, wheel: Int) : Wheel {
		{
			currentSpeed <- startSpeed;
			wheelDiameter <- wheel;
			self;
		}
	}

	setSpeed(speed: Int) : Wheel {
		{
			currentSpeed <- speed;
			self;
		}
	}
	
	isMoving() : Bool {
	 	 { true; }
	}
	
	getSpeed() : Int { { currentSpeed; }  }
	getWheelDiameter() : Int { { wheelDiameter; } }
}

class Robot {

	leftFront: Wheel <- new Wheel;
	leftBack: Wheel <- new Wheel;
	
	rightFront: Wheel <- new Wheel;
	rightBack: Wheel <- new Wheel;
	
	
	init(): Bool {
		test: Wheel <- new Wheel;
		{
			leftFront.init(0, 2);
			rightFront.init(0, 2);
			
			leftBack.init(0, 2);
			rightBack.init(0, 2);
			
			true;
		}
	}
}