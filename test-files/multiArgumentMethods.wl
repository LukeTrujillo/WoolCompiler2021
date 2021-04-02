class Exponentizer {
	pow(base: int, power: int): int {
		result: int <- 1;
		count: int <- power;
		{
			while count > 0 loop {
				result <- result * base;
				# Why would I ever write it like this? To try and trick my parser :)
				count <- -1 + count;
			} pool;
			result;
		}
	}
	
	exp(power: int): int {
		e: int <- 3 (* This approximation is good enough for engineers, so it's good enough here :) *);
		pow(e, power)
	}
}