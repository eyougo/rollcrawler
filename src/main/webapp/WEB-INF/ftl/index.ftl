<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <title>管理</title>
</head>
<body>
<div>
    <form id="start" name="start" action="${rc.contextPath}/manage/start" method="POST">
        <input type="text" id="seedUrl" name='seedUrl' value="" size="100">
        <br/>
        <input type="submit" value="START">
    </form>
</div>
<div>
    <form id="stop" name="stop" action="${rc.contextPath}/manage/stop" method="POST">
        <input type="submit" value="STOP">
    </form>
</div>
<div>
    <a href="${rc.contextPath}/manage/crawler" target="_blank">CRAWLER</a>
    <br/>
    <a href="${rc.contextPath}/manage/redis" target="_blank">REDIS</a>
</div>
</body>