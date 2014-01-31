<html>
<body>
<h1>Upload BAPLIE</h1>

<g:form controller="baplie" method="post" action="save" enctype="multipart/form-data">
    Imo Number: <input type="input" name="imoNumber" /><br>
    Baplie file: <input type="file" name="file" /><br>
    <input type="submit" />
</g:form>

</body>
</html>