package com.test.support.handler;

import com.test.common.Response;
import com.test.support.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * 控制器层全局异常处理器
 *
 * @author Shoven
 * @date  2018-11-09
 */
@ControllerAdvice
public class ControllerExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    private final static String DEFAULT_MESSAGE = "系统发生错误，请稍后再试！";

    @Value("${spring.profiles.active}")
    private String active;

    private String[] ignoredProfiles = {"dev", "test"};

    /**
     * 处理数据绑定校验异常
     *
     * @param e BindException
     * @return
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ResponseEntity handleBindException(BindException e){
        List<FieldError> fieldErrors = e.getFieldErrors();
        StringBuilder sb = new StringBuilder();
        for (FieldError fielderror : fieldErrors) {
            sb.append(fielderror.getField()).append(": ").append(fielderror.getDefaultMessage()).append(", ");
        }
        String str = sb.length() > 0 ? sb.deleteCharAt(sb.length() - 1).toString(): "请求的参数有误！";
        return Response.badRequest(str);
    }


    /**
     * 处理业务异常
     *
     * @param e BusinessException
     * @return
     */
    @ExceptionHandler(BizException.class)
    @ResponseBody
    public ResponseEntity handleBusinessException(BizException e) {
        String errorMsg = e.getMessage() != null ? e.getMessage() : DEFAULT_MESSAGE;
        return Response.unprocesable(errorMsg);
    }


    /**
     * 处理url未匹配
     *
     * @param e Exception
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public ResponseEntity handleNoHandlerException(NoHandlerFoundException e) {
        return Response.notFound(e.getRequestURL());
    }

    /**
     * 处理上传限制异常
     *
     * @param e Exception
     * @return
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public ResponseEntity handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        long b = e.getMaxUploadSize();
        String size;
        if (b == 0) {
            size = "0B";
        } else if (b < 1024) {
            size = b +"B";
        } else if (b > 1024 && b < 1024 * 1024) {
            size = b / 1024 + "KB";
        } else {
            size = b / 1024 * 1024 + "MB";
        }
        String errorMsg = "上传的文件大小超过 " + size;
        return Response.error(errorMsg);
    }

    /**
     * 处理系统级异常
     *
     * @param e Exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity handleException(Exception e) {
        logger.error(e.getMessage(), e);
        return Response.error(withDetail() ? e.getMessage() : DEFAULT_MESSAGE);
    }

    private boolean withDetail() {
        if (active == null) {
            return true;
        }
        for (String ignoredProfile : ignoredProfiles) {
            if (active.startsWith(ignoredProfile)) {
                return true;
            }
        }
        return false;
    }
}
