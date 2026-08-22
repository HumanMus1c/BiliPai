package com.android.purebilibili.data.model.response

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class DynamicOpusAuthorTimestampTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun opusDetail_acceptsStringAuthorTimestampAndKeepsInterleavedBody() {
        val payload = """
            {
              "code": 0,
              "data": {
                "item": {
                  "id_str": "1236527093179744277",
                  "type": 0,
                  "modules": [
                    {
                      "module_type": "MODULE_TYPE_TITLE",
                      "module_title": { "text": "新翼神龙卡组考卷，已快速公式答题" }
                    },
                    {
                      "module_type": "MODULE_TYPE_AUTHOR",
                      "module_author": {
                        "mid": 178991353,
                        "name": "凯尔菌",
                        "pub_time": "6天前",
                        "pub_ts": "1786739799",
                        "following": 0
                      }
                    },
                    {
                      "module_type": "MODULE_TYPE_CONTENT",
                      "module_content": {
                        "paragraphs": [
                          {
                            "para_type": 1,
                            "text": {
                              "nodes": [
                                { "word": { "words": "开门见山" } }
                              ]
                            }
                          },
                          {
                            "para_type": 2,
                            "pic": {
                              "pics": [
                                {
                                  "url": "http://i0.hdslb.com/first.jpg",
                                  "width": 1080,
                                  "height": 2400
                                }
                              ]
                            }
                          },
                          {
                            "para_type": 1,
                            "text": {
                              "nodes": [
                                { "word": { "words": "图片后的正文也必须保留" } }
                              ]
                            }
                          }
                        ]
                      }
                    }
                  ]
                }
              }
            }
        """.trimIndent()

        val item = json.decodeFromString<DynamicDetailResponse>(payload).data?.item

        assertEquals(1786739799L, item?.modules?.module_author?.pub_ts)
        assertEquals(
            listOf(
                OpusContentBlock.Text("开门见山"),
                OpusContentBlock.Image(
                    OpusPic(
                        url = "https://i0.hdslb.com/first.jpg",
                        width = 1080,
                        height = 2400
                    )
                ),
                OpusContentBlock.Text("图片后的正文也必须保留")
            ),
            item?.modules?.module_dynamic?.major?.opus?.contentBlocks
        )
    }
}
