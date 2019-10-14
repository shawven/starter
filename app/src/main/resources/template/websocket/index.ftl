<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>

</body>
<script src="/statics/js/jquery.min.js"></script>
<script src="/statics/js/socket/sockjs.min.js"></script>
<script src="/statics/js/socket/stomp.min.js"></script>
<script type="text/javascript">
    function connect() {
        stompClient = Stomp.over(new SockJS("${ctx}/websocket"));
        stompClient.connect({}, function (frame) {
            console.log(frame);
                console.log(res);
            });

            stompClient.subscribe('/user/abc/answer', function (res) {
                stompClient.send("/app/question", {}, JSON.stringify({
                    command: "close",
                    formUser: 10000,
                    toUser: 1,
                    body: {
                        id: 1
                    }
                }));
        }, function() {
            console.error("'连接服务器失败，正在重新连接...");
            // setTimeout(1000, connect())
        })
    }
    connect();
</script>
</html>
