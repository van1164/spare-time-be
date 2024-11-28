package com.van1164.resttimebe.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode (val status:Int, val code:String, val message:String) {
    // Common
    BAD_REQUEST(400, "C001", "요청 파라미터 혹은 요청 바디의 값을 다시 확인하세요."),
    INTERNAL_SERVER_ERROR(500, "C002", "Internal Server Error"),
    INVALID_INPUT_VALUE(400, "C003", "유효하지 않은 입력입니다."),
    NOT_FOUND(404, "C004", "Not Found"),
    DATETIME_INVALID(400, "C005", "유효하지 않은 날짜입니다"),
    MESSAGE_SEND_FAIL(400, "C006", "메시지 전송 실패"),

    // Auth
    USER_NOT_FOUND(404, "A001", "사용자를 찾을 수 없습니다."),
    DUPLICATED_PHONE_NUMBER(409, "A002", "이미 등록된 전화번호입니다."),

    // Oauth
    SOCIAL_EMAIL_LOAD_FAIL(400, "O001", "소셜 로그인에서 이메일을 불러올 수 없습니다."),
    SOCIAL_NAME_LOAD_FAIL(400, "O002", "소셜 로그인에서 이름을 불러올 수 없습니다."),
}
