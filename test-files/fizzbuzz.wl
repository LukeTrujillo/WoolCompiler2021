class FizzBuzz {
	run(): int { 
		n: int <- 0;
		{
			while n < 100 loop {
				if n / 3 * 3 = n then
					print("fizz")
				else
					if n / 5 * 5 = n then
						print("buzz")
					else 
						# Test the targeted method call here, too
						this.print("no")
					fi
				fi;
				n <- n + 1;
				n <- ~n;
			} pool;
			0;
		}
	}
	
	print(s: Str): Str {
		# We can't print anything yet :(
		# We will just return it for now
		s
	}
}