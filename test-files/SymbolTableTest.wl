# Classes in order
class A {
  incr(i : int) : int { i + 1}
}

class Test {
    assert(expect : int, actual : int) : boolean {
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
      a : A <- new A;
      {
          assert(2, a.incr(1));
          assert(0, a.incr(-1));
      }
    }
}
