package util

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

/** Helper class to enhance the default future from scala to allow to convert a list of futures to a list of options */
object FutureEnhancements {

  // add global execution context
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit class FutureCompanionOps(val f: Future.type) extends AnyVal {
    // idea from here: http://stackoverflow.com/questions/20874186/scala-listfuture-to-futurelist-disregarding-failed-futures
    def allAsOptions[T](fItems: /* future items */ List[Future[T]]): Future[List[Option[T]]] = {
      val listOfFutureTrys: List[Future[Option[T]]] = fItems.map(futureToFutureOption)
      Future.sequence(listOfFutureTrys)
    }

    def futureToFutureOption[T](f: Future[T]): Future[Option[T]] = {
      f.map(Some(_)).recover({ case x => None })
    }
  }


  implicit class FutureExtensions[T](f: Future[T]) {
    def mapAll[Target](m: Try[T] => Target)(implicit ec: ExecutionContext): Future[Target] = {
      val p = Promise[Target]()
      f.onComplete { r => p success m(r) }(ec)
      p.future
    }

    def flatMapAll[Target](m: Try[T] => Future[Target])(implicit ec: ExecutionContext): Future[Target] = {
      val promise = Promise[Target]()
      f.onComplete { r => m(r).onComplete { z => promise complete z }(ec) }(ec)
      promise.future
    }
  }
}
