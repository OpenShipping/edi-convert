      <div class="coarri">
        <h1>Convert Excel Load List to COARRI</h1>

        <g:form controller="excelCoarri" method="post" action="convert" enctype="multipart/form-data">
	  <table class="form">
	    <tr><td>Excel file:</td><td><input type="file" name="file"/></td></tr>
	    <tr><td>Vessel IMO:</td><td><input type="text" size="7" maxsize="7" name="vesselImo"/></td></tr> 
	    <tr><td>Vessel Name:</td><td><input type="text" name="vesselName"/></td></tr> 
	    <tr><td></td><td><input type="submit" value="Convert to COARRI 1.2"/></td></tr>
	  </table>
        </g:form>

        <h2>Documentation</h2>
        <p>
	  <a href="${resource(dir:'files',file:'coarri-template.xls')}">Detailed Load List Excel example</a>
        </p>
        <p>
	  <a href="${resource(dir:'files',file:'COARRI12.pdf')}">EDIFACT D95B COARRI (Container discharge/loading report) 1.2 manual</a>
        </p>
        <g:render template="/shared/loadlistInstructionsTemplate" />
      </div>
