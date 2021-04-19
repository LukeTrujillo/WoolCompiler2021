class Wheel {
	
	currentSpeed: int;
	wheelDiameter: int;

	init(startSpeed: int, wheel: int) : Wheel {
		{
			currentSpeed <- startSpeed;
			wheelDiameter <- wheel;
			this;
		}
	}

	setSpeed(speed: int) : Wheel {
		{
			currentSpeed <- speed;
			this;
		}
	}
	
	isMoving() : boolean {
	 	 { true; }
	}
	
	getSpeed() : int { currentSpeed }
	getWheelDiameter() : int { wheelDiameter }
}

class Robot {

	leftFront: Wheel <- new Wheel;
	leftBack: Wheel <- new Wheel;
	
	rightFront: Wheel <- new Wheel;
	rightBack: Wheel <- new Wheel;
	
	
	init(): boolean {
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