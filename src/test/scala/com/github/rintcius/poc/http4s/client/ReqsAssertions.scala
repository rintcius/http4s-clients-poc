package com.github.rintcius.poc.http4s.client

import cats.effect.IO
import munit.Assertions
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status

object ReqsAssertions extends Assertions {

  def assertStatusAndResponseContents(
      status: Status,
      respBodyPredicate: String => Boolean
  ): (Request[IO], Response[IO]) => IO[Unit] = { (req, resp) =>
    resp.as[String].map { respBody =>
      assert(
        respBodyPredicate(respBody),
        s"Unexpected response: '$respBody'"
      )
      assertEquals(resp.status, status)
    }
  }

}
