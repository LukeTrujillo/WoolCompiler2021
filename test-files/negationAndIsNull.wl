class BooleanExpressions {
	doSomething(): Str {
		obj: Object;
		isThisNull: boolean <- isnull obj;
		# spoiler alert, this should return that it's null
		if ~isThisNull then "Is not null, for some reason" else "It's null!" fi
	}
}