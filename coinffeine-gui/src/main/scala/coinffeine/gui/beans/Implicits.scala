package coinffeine.gui.beans

import scala.language.implicitConversions

import java.util.concurrent.Callable
import javafx.beans.binding._
import javafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer

import coinffeine.common.properties.{PropertyMap, Property}

object Implicits {

  implicit def pimpMyObservableValue[A](observableValue: ObservableValue[A]): ObservableValuePimp[A] =
    new ObservableValuePimp(observableValue)

  implicit def pimpMyProperty[A](property: Property[A]): PropertyPimp[A] =
    new PropertyPimp(property)

  implicit def pimpMyPropertyMap[K, V](property: PropertyMap[K, V]): PropertyMapPimp[K, V] =
    new PropertyMapPimp(property)

  implicit def pimpMyObservableBuffer[A](buffer: ObservableBuffer[A]): ObservableBufferPimp[A] =
    new ObservableBufferPimp(buffer)

  implicit def pimpMyObjectBinding[A](binding: ObjectBinding[A]): ObjectBindingPimp[A] =
    new ObjectBindingPimp(binding)

  implicit def pimpMyBooleanBinding(binding: BooleanBinding): BooleanBindingPimp =
    new BooleanBindingPimp(binding)

  implicit def pimpMyDoubleBinding(binding: DoubleBinding): DoubleBindingPimp =
    new DoubleBindingPimp(binding)

  implicit def pimpMyStringBinding(binding: StringBinding): StringBindingPimp =
    new StringBindingPimp(binding)

  def createObjectBinding[A, B, C](a: ObservableValue[A], b: ObservableValue[B])
                                  (map: (A, B) => C): ObjectBinding[C] =
    Bindings.createObjectBinding(new Callable[C] {
      override def call() = map(a.getValue, b.getValue)
    }, a, b)
}
