class Precedence {
	run(): boolean {
		~(1 + 2 * (3 + 4) < 4 * 5 + 3 / 4 - 1)
	}
	
	notRun(): boolean {
		# Is used to check that ~ has lower precedence than ., and that assignment is lower than either
		result: boolean <- ~this.run();
		result
	}
}