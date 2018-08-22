package com.iiot.stream.tools

class CustomIterator(iter:Iterator[Int]) extends Iterator[Int]{
  override def hasNext = {
    iter.hasNext
  }

  override def next() = {
    iter.next()
  }
}
