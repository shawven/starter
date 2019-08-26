package com.test.app.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.HttpStatus.*;

/**
 * 自定义响应消息体
 * 提供一些静态方法封装 ResponseEntity 以适应RestFull风格API，会改变http status
 *
 * @author Shoven
 * @date 2019-07-10 14:27
 */
public class Response {
    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private Object data;

    public Response() {
    }

    public Response(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public Response setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Response setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Response setData(Object data) {
        this.data = data;
        return this;
    }

    /**
     * 200 OK - [GEObject]：服务器成功返回用户请求的数据，该操作是幂等的（Idempotent）
     *
     * @return 响应体
     */
    public static ResponseEntity<Response> ok() {
        return ok(OK.getReasonPhrase());
    }

    /**
     * @param msg 消息
     * @return    响应体
     */
    public static ResponseEntity<Response> ok(String msg) {
        return ok(msg, null);
    }

    /**
     * @param data 数据
     * @return    响应体
     */
    public static ResponseEntity<Response> ok(Object data) {
        return ok(OK.getReasonPhrase(), data);
    }

    /**
     * @param msg 消息
     * @param data 数据
     * @return    响应体
     */
    public static ResponseEntity<Response> ok(String msg, Object data) {
        return build(OK, 0, msg, data);
    }

    /**
     * @param code 状态码
     * @param msg 消息
     * @param data 数据
     * @return    响应体
     */
    public static ResponseEntity<Response> ok(int code, String msg, Object data) {
        return build(OK, code, msg, data);
    }

    /**
     * 201 Created - [POSObject/PUObject/PAObjectCH]：用户新建或修改数据成功
     *
     * @return 响应体
     */
    public static ResponseEntity created() {
        return created(CREATED.getReasonPhrase());
    }

    /**
     * @param msg 消息
     * @return    响应体
     */
    public static ResponseEntity<Response> created(String msg) {
        return created(msg, null);
    }

    /**
     * @param data 数据
     * @return    响应体
     */
    public static ResponseEntity<Response> created(Object data) {
        return created(CREATED.getReasonPhrase(), data);
    }

    /**
     * @param msg 消息
     * @param data 数据
     * @return    响应体
     */
    public static ResponseEntity<Response> created(String msg, Object data) {
        return build(CREATED, 0, msg, data);
    }

    /**
     * 202 Accepted - [*]：表示一个请求已经进入后台排队（异步任务）
     *
     * @return 响应体
     */
    public static ResponseEntity accepted() {
        return accepted(null);
    }

    /**
     * @param msg 消息
     * @return   响应体
     */
    public static ResponseEntity accepted(String msg) {
        return accepted(msg, null);
    }

    /**
     * @param data 数据
     * @return   响应体
     */
    public static ResponseEntity accepted(Object data) {
        return accepted(ACCEPTED.getReasonPhrase(), data);
    }

    /**
     * @param msg 消息
     * @param data 数据
     * @return   响应体
     */
    public static ResponseEntity accepted(String msg, Object data) {
        return build(ACCEPTED, 0, msg, data);
    }

    /**
     * 204 No Content - [DELEObjectE]：用户删除数据成功，无数据返回
     *
     * @return 响应体
     */
    public static ResponseEntity noContent() {
        return build(NO_CONTENT, 0, NO_CONTENT.getReasonPhrase());
    }

    /**
     * 400 Bad Request - [*]：请求参数有误
     *
     * @return 响应体
     */
    public static ResponseEntity badRequest() {
        return badRequest(BAD_REQUEST.getReasonPhrase());
    }

    /**
     * @param msg 消息
     * @return    响应体
     */
    public static ResponseEntity badRequest(String msg) {
        return build(BAD_REQUEST, BAD_REQUEST.value(), msg);
    }

    /**
     * 401 Unauthorized - [*]：表示用户没有权限（令牌、用户名、密码错误）
     *
     * @return 响应体
     */
    public static ResponseEntity unauthorized () {
        return unauthorized(UNAUTHORIZED.getReasonPhrase());
    }

    /**
     * 401 Unauthorized - [*]：表示用户没有权限（令牌、用户名、密码错误）
     *
     * @return 响应体
     */
    public static ResponseEntity unauthorized (String msg) {
        return unauthorized(UNAUTHORIZED.value(), msg);
    }

    /**
     * 401 Unauthorized - [*]：表示用户没有权限（令牌、用户名、密码错误）
     *
     * @return 响应体
     */
    public static ResponseEntity unauthorized (int code, String msg) {
        return build(UNAUTHORIZED, code, msg);
    }

    /**
     * 403 Forbidden - [*] 表示用户得到授权（与401错误相对），但是访问是被禁止的
     *
     * @return 响应体
     */
    public static ResponseEntity forbidden () {
        return forbidden(FORBIDDEN.getReasonPhrase());
    }

    /**
     * 403 Forbidden - [*] 表示用户得到授权（与401错误相对），但是访问是被禁止的
     *
     * @return 响应体
     */
    public static ResponseEntity forbidden (String msg) {
        return forbidden(FORBIDDEN.value(), msg);
    }

    /**
     * 403 Forbidden - [*] 表示用户得到授权（与401错误相对），但是访问是被禁止的
     *
     * @param code 状态码
     * @return 响应体
     */
    public static ResponseEntity forbidden (int code, String msg) {
        return build(FORBIDDEN, code, msg);
    }

    /**
     * 404 Not Found - [*]：用户发出的请求针对的是不存在的记录，服务器没有进行操作，该操作是幂等的。
     *
     * @return 响应体
     */
    public static ResponseEntity notFound() {
        return notFound(NOT_FOUND.getReasonPhrase());
    }

    /**
     * @param msg 消息
     * @return    响应体
     */
    public static ResponseEntity notFound(String msg) {
        return build(NOT_FOUND, NOT_FOUND.value(), msg);
    }

    /**
     * 406 Not Acceptable - [GEObject]：用户请求的格式不可得（比如用户请求JSON格式，但是只有XML格式）。
     *
     * @return 响应体
     */
    public static ResponseEntity notAcceptable() {
        return notAcceptable(NOT_ACCEPTABLE.getReasonPhrase());
    }

    /**
     * 406 Not Acceptable - [GEObject]：用户请求的格式不可得（比如用户请求JSON格式，但是只有XML格式）。
     *
     * @return 响应体
     */
    public static ResponseEntity notAcceptable(String msg) {
        return build(NOT_ACCEPTABLE, NOT_ACCEPTABLE.value(), msg);
    }

    /**
     * 422  Unprocesable entity - [POSObject/PUObject/PAObjectCH] 当创建一个对象时，发生一个验证错误，语义错误，无法响应。
     *
     * @return 响应体
     */
    public static ResponseEntity unprocesable() {
        return unprocesable(UNPROCESSABLE_ENTITY.getReasonPhrase());
    }

    /**
     * @param msg 消息
     * @return    响应体
     */
    public static ResponseEntity unprocesable(String msg) {
        return unprocesable(UNPROCESSABLE_ENTITY.value(), msg);
    }

    /**
     * @param code 状态码
     * @param msg 消息
     * @return    响应体
     */
    public static ResponseEntity unprocesable(int code, String msg) {
        return build(UNPROCESSABLE_ENTITY, code, msg);
    }

    /**
     * 500 Internal Server Error - [*]：服务器发生错误，用户将无法判断发出的请求是否成功。
     *
     * @param code 状态码
     * @return     响应体
     */
    public static ResponseEntity<Response> error(int code) {
        return error(code, INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    /**
     * 500 Internal Server Error - [*]：服务器发生错误，用户将无法判断发出的请求是否成功。
     *
     * @param msg 消息
     * @return    响应体
     */
    public static ResponseEntity<Response> error(String msg) {
        return error(INTERNAL_SERVER_ERROR.value(), msg);
    }

    /**
     * @param code 状态码
     * @param msg  消息
     * @return    响应体
     */
    public static ResponseEntity<Response> error(int code, String msg) {
        return build(INTERNAL_SERVER_ERROR, code, msg);
    }

    /**
     * 构建响应体
     *
     * @param status  状态
     * @param msg     消息
     * @return        响应体
     */
    public static ResponseEntity<Response> build(HttpStatus status, String msg) {
        return build(status, status.value(), msg, null);
    }

    /**
     * 构建响应体
     *
     * @param status  状态
     * @param code    状态码
     * @param msg     消息
     * @return        响应体
     */
    public static  ResponseEntity<Response> build(HttpStatus status, int code, String msg) {
        return build(status, code, msg, null);
    }

    /**
     * 构建响应体
     *
     * @param status  状态
     * @param code    状态码
     * @param msg     消息
     * @param data    数据
     * @return        响应体
     */
    public static  ResponseEntity<Response> build(HttpStatus status, int code, String msg, Object data) {
        return build(status, null, code, msg, data);
    }

    /**
     * 构建响应体
     *
     * @param status  HTTP状态码
     * @param headers HTTP头部
     * @param code    状态码
     * @param msg     消息
     * @param data    数据
     * @return        响应体
     */
    public static ResponseEntity<Response> build(HttpStatus status, HttpHeaders headers,
                                                 int code, String msg, Object data) {
        Response response = new Response(code, msg, data);
        return new ResponseEntity<>(response, headers, status);
    }

    /**
     * 构建响应体
     *
     * @param status   HTTP状态码
     * @param response 自定义response对象
     * @return         响应体
     */
    public static ResponseEntity<Response> build(HttpStatus status, Response response) {
        return build(status, null, response);
    }

    /**
     * 构建响应体
     *
     * @param status   HTTP状态码
     * @param headers  HTTP头部
     * @param response 自定义response对象
     * @return         响应体
     */
    public static ResponseEntity<Response> build(HttpStatus status, HttpHeaders headers, Response response) {
        return new ResponseEntity<>(response, headers, status);
    }
}