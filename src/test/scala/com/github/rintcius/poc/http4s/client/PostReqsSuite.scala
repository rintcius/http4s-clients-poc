package com.github.rintcius.poc.http4s.client

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.syntax.all._
import io.circe.Json
import munit.Assertions
import munit.CatsEffectSuite
import munit.FunSuite
import org.asynchttpclient.AsyncHttpClientConfig
import org.http4s.Header
import org.http4s.Headers
import org.http4s.MediaType
import org.http4s.Method
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.asynchttpclient.client.AsyncHttpClient
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.jetty.client.JettyClient
import org.http4s.okhttp.client.OkHttpBuilder
import org.http4s.syntax.all._
import org.http4s.syntax.header

class AsyncHttpPostReqsSuite extends PostReqsSuite(AsyncHttpClient.resource[IO]())

class BlazePostReqsSuite extends PostReqsSuite(BlazeClientBuilder[IO].resource)

class EmberPostReqsSuite extends PostReqsSuite(EmberClientBuilder.default[IO].build)

class JettyPostReqsSuite extends PostReqsSuite(JettyClient.resource[IO]())

class OkHttpPostReqsSuite
    extends PostReqsSuite(OkHttpBuilder.withDefaultClient[IO].flatMap(_.resource))

case class TestCase(
    name: String,
    req: Request[IO],
    assertion: (Request[IO], Response[IO]) => IO[Unit]
)

abstract class PostReqsSuite(
    mkClient: Resource[IO, Client[IO]]
) extends CatsEffectSuite {

  val testCases = List(
    TestCase(
      "POST Simple",
      Request[IO](Method.POST, uri"https://httpbin.org/post"),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": """"")
      )
    ),
    // *****************
    // withEntity String
    // *****************
    TestCase(
      "POST withEntity String",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withEntity("some body"),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "some body"""")
      )
    ),
    // Note: data is empty for async-http and okhttp
    TestCase(
      "POST withEntity String then withHeaders text/plain",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withEntity("some body")
        .withHeaders(
          org.http4s.headers.`Content-Type`(MediaType.text.plain)
        ),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "some body"""")
      )
    ),
    TestCase(
      "POST withEntity String then putHeaders text/plain",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withEntity("some body")
        .putHeaders(
          org.http4s.headers.`Content-Type`(MediaType.text.plain)
        ),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "some body"""")
      )
    ),
    TestCase(
      "POST withHeaders text/plain then withEntity String",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withHeaders(
          org.http4s.headers.`Content-Type`(MediaType.text.plain)
        )
        .withEntity("some body"),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "some body"""")
      )
    ),
    TestCase(
      "POST putHeaders text/plain then withEntity String",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .putHeaders(
          org.http4s.headers.`Content-Type`(MediaType.text.plain)
        )
        .withEntity("some body"),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "some body"""")
      )
    ),
    // **********************
    // withEntity Byte Stream
    // **********************
    TestCase(
      "POST withEntity Byte Stream",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withEntity(fs2.Stream.emits("some body".getBytes()).covary[IO]),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "some body"""")
      )
    ),
    // Note: data is empty for async-http and okhttp
    TestCase(
      "POST withEntity Byte Stream then withHeaders text/plain",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withEntity(fs2.Stream.emits("some body".getBytes()).covary[IO])
        .withHeaders(
          org.http4s.headers.`Content-Type`(MediaType.text.plain)
        ),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "some body"""")
      )
    ),
    TestCase(
      "POST withEntity Byte Stream then putHeaders text/plain",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withEntity(fs2.Stream.emits("some body".getBytes()).covary[IO])
        .putHeaders(
          org.http4s.headers.`Content-Type`(MediaType.text.plain)
        ),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "some body"""")
      )
    ),
    TestCase(
      "POST withHeaders text/plain then withEntity Byte Stream",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withHeaders(
          org.http4s.headers.`Content-Type`(MediaType.text.plain)
        )
        .withEntity(fs2.Stream.emits("some body".getBytes()).covary[IO]),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "some body"""")
      )
    ),
    TestCase(
      "POST putHeaders text/plain then withEntity Byte Stream",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .putHeaders(
          org.http4s.headers.`Content-Type`(MediaType.text.plain)
        )
        .withEntity(fs2.Stream.emits("some body".getBytes()).covary[IO]),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "some body"""")
      )
    ),
    // ***************
    // withEntity Json
    // ***************
    TestCase(
      "POST withEntity Json",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withEntity(Json.fromString("some body")),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "\"some body\""""")
      )
    ),
    // Note: data is empty for async-http and okhttp
    TestCase(
      "POST withEntity Json then withHeaders application/json",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withEntity(Json.fromString("some body"))
        .withHeaders(
          org.http4s.headers.`Content-Type`(MediaType.application.json)
        ),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "\"some body\""""")
      )
    ),
    TestCase(
      "POST withEntity Json then putHeaders application/json",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withEntity(Json.fromString("some body"))
        .putHeaders(
          org.http4s.headers.`Content-Type`(MediaType.application.json)
        ),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "\"some body\""""")
      )
    ),
    TestCase(
      "POST withHeaders application/json then withEntity Json",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .withHeaders(
          org.http4s.headers.`Content-Type`(MediaType.application.json)
        )
        .withEntity(Json.fromString("some body")),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "\"some body\""""")
      )
    ),
    TestCase(
      "POST putHeaders application/json then withEntity Json",
      Request[IO](Method.POST, uri"https://httpbin.org/post")
        .putHeaders(
          org.http4s.headers.`Content-Type`(MediaType.application.json)
        )
        .withEntity(Json.fromString("some body")),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""data": "\"some body\""""")
      )
    )
  )

  testCases.foreach { testCase =>
    val req = testCase.req

    test(testCase.name) {

      mkClient.use { client =>
        client.run(req).use { resp =>
          testCase.assertion(req, resp)
        }
      }
    }
  }
}

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
