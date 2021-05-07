 class Wheel inherits IO {
	assert(expect : boolean, actual : boolean) : boolean {
        if expect = actual
        then
            true
        else
        {
            abort();
            false;
        }
        fi
    }

    run() : boolean {
        {
            assert(true, true = 1 > 0);
            assert(false, true = true = false);
        }
    }
}