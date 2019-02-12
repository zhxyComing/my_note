package com.app.xuzheng.mynote.Utils

import com.app.xuzheng.mynote.Bean.NoteInfo

/**
 * Created by xuzheng on 2017/9/12.
 */
class SortByNoteTime : Comparator<NoteInfo> {
    //这个地方应该有更好的kotlin写法
    override fun compare(note_1: NoteInfo, note_2: NoteInfo): Int {

        val date_1 = TimeUtils.stringToLong(note_1.fileName, "yy-MM-dd-HH-mm-ss")
        val date_2 = TimeUtils.stringToLong(note_2.fileName, "yy-MM-dd-HH-mm-ss")

        return date_1.compareTo(date_2)
    }
}