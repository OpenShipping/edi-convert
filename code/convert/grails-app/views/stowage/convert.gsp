<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

  <g:render template="/shared/html_head"/>

  <body>

    <g:render template="/shared/head"/>

    <div id="main">

      <div class="stowage">
	<h1>Stowage was converted</h1>
	<p class="ok"><a href="downloadStowage">Download Stowage (.sto) ${session.jsonFileName}</a></p>
	<pre>${session.result.messages.status}</pre>
      </div>
    
      <g:render template="/shared/stowageUpload"/>

    </div>

    <g:render template="/shared/foot"/>

  </body>
</html>
