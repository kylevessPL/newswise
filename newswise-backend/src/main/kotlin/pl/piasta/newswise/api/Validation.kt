package pl.piasta.newswise.api

import io.github.cdimascio.openapi.Validate
import org.springframework.web.server.ResponseStatusException

private const val OPENAPI_SPEC_PATH = "api.yml"

val validate = Validate.configure(OPENAPI_SPEC_PATH) { _, status, messages ->
    throw ResponseStatusException(status, messages.first())
}
