package wi.co.timetracker.service

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val mapper: ObjectMapper = ObjectMapper()
  .registerKotlinModule()
  .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
  .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
  .configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true)
  .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
  .configure(SerializationFeature.INDENT_OUTPUT, true)
  .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
