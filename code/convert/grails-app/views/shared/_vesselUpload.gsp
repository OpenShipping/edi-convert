      <div class="vessel">
	<h1>Convert Excel Vessel Profile</h1>

	<g:form controller="vesselParser" method="post" action="convert" enctype="multipart/form-data">
	  <table class="form">
	    <tr><td>Excel file:</td><td><input type="file" name="file"/></td></tr>
	    <tr><td></td><td><input type="submit" value="Convert"/></td></tr>
	  </table>
	</g:form>

        <h2>Documentation</h2>
        <ul>
          <li><a href="format">Description of format</a>
          <li><a href="${resource(dir:'files',file:'vessel-template.xls')}">Vessel profile Excel example</a>
        </ul>
      </div>
