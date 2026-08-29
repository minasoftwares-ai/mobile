package com.example.model

enum class QuestionType(val rawValue: String) {
    MCQ("MCQ"),
    TRUE_FALSE("TF"),
    EXPLAIN_TEXT("EXPLAIN");

    companion object {
        fun fromString(value: String?): QuestionType {
            return when (value?.uppercase()) {
                "MCQ", "MULTIPLE_CHOICE", "CHOICE" -> MCQ
                "TF", "TRUE_FALSE", "BOOLEAN", "SAH_KHATA" -> TRUE_FALSE
                "EXPLAIN", "TEXT", "BEM_TOFASSER", "ESSAY" -> EXPLAIN_TEXT
                else -> MCQ
            }
        }
    }
}
