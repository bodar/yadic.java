package com.googlecode.yadic

import java.util.Arrays.asList

class ContainerException(message:String, causes:List[ContainerException]) extends RuntimeException(message, causes.last){
  def this(message:String) = this(message, List(null) )

  def getCauses:java.util.List[ContainerException] = asList(causes.toArray: _*)
}