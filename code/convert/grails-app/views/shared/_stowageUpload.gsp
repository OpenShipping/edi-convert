      <div class="stowage">
	<h1>Convert Excel Stowage</h1>

	<g:form controller="stowage" method="post" action="convert" enctype="multipart/form-data">
	  <table class="form">
	    <tr><td>Excel file:</td><td><input type="file" name="file"/></td></tr>
	    <tr><td></td><td><input type="submit" value="Convert"/></td></tr>
	  </table>
	</g:form>
      
	<h2>Format of MS-Excel file</h2>
	<p>
	  <a href="${resource(dir:'files',file:'stowage-template.xls')}">Stowage Excel example</a>
	</p>

	<g:render template="/shared/loadlistInstructionsTemplate" />

	<h3>Sheet 'Schedule'</h3>
	<p>
	  Ignores the first row, creates a call in the schedule for each row. Columns after A are ignored.
	</p>

	<table class="doc">
	  <tr> <th>Column</th> <th>Place</th> <th>Field name</th> <th>Status</th> <th>Content type</th> <th>Example</th> <th>Comment</th> </tr>
	  <!-- A  0 PORT_CODE -->
	  <tr> <td>A</td> <td>0</td>  <td>PORT_CODE</td> <td>M</td> <td>STRING</td>       <td>USLAX</td> <td>UN/LOCODE</td> </tr>
	</table>

	<h3>Sheet 'Vessel'</h3>
	<p>
	  Ignores the first row, only reads second row. Columns after A are ignored.
	</p>
	
	<table class="doc">
	  <tr> <th>Column</th> <th>Place</th> <th>Field name</th> <th>Status</th> <th>Content type</th> <th>Example</th> <th>Comment</th> </tr>
	  <!-- A  0 IMO -->
	  <tr> <td>A</td> <td>0</td>  <td>IMO</td> <td>M</td> <td>STRING</td>       <td></td> <td>IMO number of vessel</td> </tr>
	</table>

      </div>
