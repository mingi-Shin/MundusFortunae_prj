package com.mingisoft.mf.api;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 보통 2xx(200대) 응답용
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

  //상태코드를 HTTP와 body에서도 관리하려고.. 왜? 프론트엔드에서 body에 넣어달라 할수도 있자나. 
  private int statusCode;    // HTTP status code (200, 201, 204...)
  private String message;
  private T data;
  
  /**
   * 가장 기본: 200(고정) + "success"
   */
  public static <T> ApiResponse<T> ok(T data) {
    return of(HttpStatus.OK, "success", data);
  }

  /**
   * 200(고정) + 커스텀 메시지
   */
  public static <T> ApiResponse<T> ok(String message, T data) {
    return of(HttpStatus.OK, message, data);
  }

  /**
   * 생성(201)(고정) 같은 다른 2xx에도 쓰기 쉽게
   */
  public static <T> ApiResponse<T> created(T data) {
    return of(HttpStatus.CREATED, "created", data);
  }
  
  /**
   * ... 그 밖의 여러가지 200대 응답
   */

  /**
   * status(내맘대로 커스텀) + 팩토리 메서드! 난 이게 제일 맘에 드네 
   */
  public static <T> ApiResponse<T> of(HttpStatus status, String message, T data) {
    return ApiResponse.<T>builder()
        .statusCode(status.value()) // 여기서 int로 바꿔서 넣음 (200, 201 ...)
        .message(message)
        .data(data)
        .build();
  }
  
}
