package com.github.rintcius.poc.http4s.client

import cats.effect.IO
import org.http4s.Request
import org.http4s.Response

case class TestCase(
    name: String,
    req: Request[IO],
    assertion: (Request[IO], Response[IO]) => IO[Unit]
)
