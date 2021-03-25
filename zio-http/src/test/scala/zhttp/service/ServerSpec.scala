package zhttp.service

import zhttp.http._
import zhttp.service.server.ServerChannelFactory
import zio.test.assertM
import zio.test.Assertion.equalTo
import zio.ZIO

object ServerSpec extends HttpRunnableSpec {
  val env = EventLoopGroup.auto() ++ ChannelFactory.auto ++ ServerChannelFactory.auto

  val app = serve {
    Http.collectM[Request] {
      case Method.GET -> Root / "success" => ZIO.succeed(Response.ok)
      case Method.GET -> Root / "failure" => ZIO.fail(new Throwable("FAILURE"))
    }
  }

  def spec = suiteM("Server")(
    app
      .as(
        List(
          testM("200 response") {
            val actual = status(Root / "success")
            assertM(actual)(equalTo(Status.OK))
          },
          testM("500 response") {
            val actual = status(Root / "failure")
            assertM(actual)(equalTo(Status.INTERNAL_SERVER_ERROR))
          },
          testM("404 response") {
            val actual = status(Root / "random")
            assertM(actual)(equalTo(Status.NOT_FOUND))
          },
        ),
      )
      .useNow,
  ).provideCustomLayer(env)
}
