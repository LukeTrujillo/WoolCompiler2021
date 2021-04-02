class BinarySearchTree {
	left: BinarySearchTree;
	right: BinarySearchTree;
	value: int;
	
	(* Sets the value of the current node.
	   Is a bit silly in that it does not recompute the children. Doing this properly is an exercise for the caller :)
	 *)
	setValue(v: int): int {
		{
			value <- v;
			value;
		}
	}
	
	setChildValue(v: int): int {
		select 
			v <= value: {left = new BinarySearchTree; left.setValue(v);};
			v > value: {right = new BinarySearchTree; right.setValue(v);};
		end
	}
}