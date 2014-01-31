      <div class="coprar">
        <h1>Convert Excel Load List to COPRAR</h1>

        <g:form controller="coprar" method="post" action="convert" enctype="multipart/form-data">
	  <table class="form">
	    <tr><td>Excel file:</td><td><input type="file" name="file"/></td></tr>
	    <tr><td>Vessel IMO:</td><td><input type="text" size="7" maxsize="7" name="vesselImo"/></td></tr> 
	    <tr><td>Vessel Name:</td><td><input type="text" name="vesselName"/></td></tr> 
	    <tr><td></td><td><input type="submit" value="Convert to COPRAR 1.2"/></td></tr>
	  </table>
        </g:form>

        <h2>Documentation</h2>
        <p>
	  <a href="${resource(dir:'files',file:'coprar-template.xls')}">Detailed Load List Excel example</a>
        </p>
        <p>
	  <a href="${resource(dir:'files',file:'projections-template.xls')}">Projected Load List Excel example</a>
        </p>
        <p>
	  <a href="${resource(dir:'files',file:'COPRAR12.pdf')}">EDIFACT D95B 1996-10 SMDG COPRAR 1.2 manual</a>
	  <!-- <a href="${resource(dir:'files',file:'COPRAR20.pdf')}">EDIFACT D00B 2003-03 SMDG COPRAR 2.0 manual</a> -->
        </p>
        <g:render template="/shared/loadlistInstructionsTemplate" />
      </div>
