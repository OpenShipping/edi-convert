      <div class="baplie">
        <h1>Convert Excel Load List to BAPLIE</h1>

        <g:form controller="excelBaplie" method="post" action="convert" enctype="multipart/form-data">
	  <table class="form">
	    <tr><td>Excel file:</td><td><input type="file" name="file"/></td></tr>
	    <tr><td>Vessel IMO:</td><td><input type="text" size="7" maxsize="7" name="vesselImo"/></td></tr> 
	    <tr><td>Vessel Name:</td><td><input type="text" name="vesselName"/></td></tr> 
	    <tr><td></td><td><input type="submit" value="Convert to BAPLIE 2.1"/></td></tr>
	  </table>
        </g:form>

        <h2>Documentation</h2>
        <p>
	  <a href="${resource(dir:'files',file:'baplie-template.xls')}">Load List Excel example with Slot Positions</a>
        </p>
        <p>
	  <a href="${resource(dir:'files',file:'BAPLIE211.pdf')}">EDIFACT D00B 2003-03 SMDG BAPLIE 2.1 manual</a>
        </p>
        <g:render template="/shared/loadlistInstructionsTemplate" />
      </div>