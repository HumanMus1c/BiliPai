package com.android.purebilibili.data.repository

import com.android.purebilibili.data.model.response.LiveRoom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LiveAreaRoomsPolicyTest {

    @Test
    fun `risk control and rate limit errors should trigger live area fallback`() {
        assertTrue(shouldFallbackLiveAreaRooms(code = -352, message = ""))
        assertTrue(shouldFallbackLiveAreaRooms(code = -412, message = ""))
        assertTrue(shouldFallbackLiveAreaRooms(code = -509, message = ""))
        assertTrue(shouldFallbackLiveAreaRooms(code = 22015, message = ""))
        assertFalse(shouldFallbackLiveAreaRooms(code = -404, message = ""))
    }

    @Test
    fun `fallback room filter prefers exact area name matches`() {
        val rooms = listOf(
            LiveRoom(roomid = 1, areaName = "英雄联盟", title = "lol"),
            LiveRoom(roomid = 2, areaName = "无畏契约", title = "valorant"),
            LiveRoom(roomid = 3, areaName = "无畏契约", title = "rank")
        )

        val filtered = filterFallbackLiveAreaRooms(rooms, areaTitle = "无畏契约")

        assertEquals(listOf(2L, 3L), filtered.map { it.roomid })
    }

    @Test
    fun `fallback room filter can fall back to title matches`() {
        val rooms = listOf(
            LiveRoom(roomid = 1, areaName = "网游", title = "无畏契约排位"),
            LiveRoom(roomid = 2, areaName = "网游", title = "英雄联盟")
        )

        val filtered = filterFallbackLiveAreaRooms(rooms, areaTitle = "无畏契约")

        assertEquals(listOf(1L), filtered.map { it.roomid })
    }


    @Test
    fun `room pagination uses total count when has more flag is absent`() {
        assertTrue(
            hasMoreLiveAreaRooms(
                loadedCount = 30,
                page = 1,
                pageSize = 30,
                hasMoreFlag = null,
                totalCount = 438
            )
        )
        assertFalse(
            hasMoreLiveAreaRooms(
                loadedCount = 18,
                page = 15,
                pageSize = 30,
                hasMoreFlag = null,
                totalCount = 438
            )
        )
    }

    @Test
    fun `explicit final page wins over a full page and stale total count`() {
        assertFalse(hasMoreLiveAreaRooms(30, 1, 30, 0, 438))
        assertFalse(hasMoreLiveAreaRooms(30, 1, 30, 0, 0))
        assertTrue(hasMoreLiveAreaRooms(30, 15, 30, 1, 438))
    }

    @Test
    fun `missing pagination metadata uses page size while empty pages always stop`() {
        assertTrue(hasMoreLiveAreaRooms(30, 1, 30, null, 0))
        assertFalse(hasMoreLiveAreaRooms(29, 1, 30, null, 0))
        assertFalse(hasMoreLiveAreaRooms(0, 1, 30, 1, 438))
    }
}
