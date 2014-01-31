<html>
<body>
<h1>Upload TCC Excel loadlist</h1>

<g:form controller="tccExcel" method="post" action="save" enctype="multipart/form-data">
    Imo Number: <input type="input" name="imoNumber" /><br>
    TCC Excel file: <input type="file" name="file" /><br>
    <input type="submit" />
</g:form>

<span style="font-size:20pt; color:red">NOTICE! You might want to use <a href="../stowage">the new stowage converter</a>.</span>

</body>
</html>
