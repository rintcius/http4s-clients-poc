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

class AsyncHttpGetReqsSuite extends GetReqsSuite(AsyncHttpClient.resource[IO]())

class BlazeGetReqsSuite extends GetReqsSuite(BlazeClientBuilder[IO].resource)

class EmberGetReqsSuite
    extends GetReqsSuite(EmberClientBuilder.default[IO].build)

class JettyGetReqsSuite extends GetReqsSuite(JettyClient.resource[IO]())

class OkHttpGetReqsSuite
    extends GetReqsSuite(
      OkHttpBuilder.withDefaultClient[IO].flatMap(_.resource)
    )

abstract class GetReqsSuite(
    mkClient: Resource[IO, Client[IO]]
) extends CatsEffectSuite {

  val testCases = List(
    TestCase(
      "GET Simple",
      Request[IO](Method.GET, uri"https://httpbin.org/get"),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""url": "https://httpbin.org/get"""")
      )
    ),
    // *****************
    // withEntity String
    // *****************
    // Fails for okhttp
    // java.lang.IllegalArgumentException: method GET must not have a request body.
    TestCase(
      "GET withEntity String",
      Request[IO](Method.GET, uri"https://httpbin.org/get")
        .withEntity("some body"),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""url": "https://httpbin.org/get"""")
      )
    ),
    // Fails for okhttp
    // java.lang.IllegalArgumentException: method GET must not have a request body.
    TestCase(
      "GET withEntity empty String",
      Request[IO](Method.GET, uri"https://httpbin.org/get")
        .withEntity(""),
      ReqsAssertions.assertStatusAndResponseContents(
        Status.Ok,
        _.contains(""""url": "https://httpbin.org/get"""")
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
