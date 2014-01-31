<h3>Sheet 'Load list'</h3>

<p>
The 'Load list' parser reads the first cell in the first row of the
sheet. The value 'CONTAINER_ID' identifies a detailed load list, whereas the value 'CONTAINER_COUNT' is taken as a projected load list,
which will be expanded into individual containers. All columns after column M are ignored. 
</p>
<p>
Columns/Fields have fixed order, they must appear in the order as
indicated in the following table. The status of a field is either M=mandatory, O=optional, or D=dependent if required
in the presence or absence of other fields.
</p>

<table class="doc">
<tr> <th>Column</th> <th>Place</th> <th>Field name</th> <th>Status</th> <th>Content type</th> <th>Example</th> <th>Comment</th> </tr>
<!-- A  0 CONTAINER_ID (COPRAR) -->
<tr> <td>A</td> <td>0</td>  <td>CONTAINER_ID</td> <td>M</td> <td>STRING, ISO 6346</td>       <td>ANGU1234565</td> <td>BAPLIE, COARRI and COPRAR detailed on-board and load list only, rows without equipment CONTAINER_ID are skipped silently</td> </tr>
<!-- A  0 CONTAINER_COUNT (PROJECTIONS) -->
<tr> <td>A</td> <td>0</td>  <td>CONTAINER_COUNT</td> <td>M</td> <td>INT</td>                    <td>37</td> <td>COPRAR projected load list only, rows without number of estimated CONTAINER_COUNT are skipped silently, otherwise each row is expanded into CONTAINER_COUNT containers</td> </tr>
<!-- B  1 VOYAGE_NO -->
<tr> <td>B</td> <td>1</td>  <td>VOYYAGE_NO</td>   <td>M</td> <td>STRING</td>                 <td>V03746</td> <td>any text possible</td> </tr>
<!-- C  2 POL -->
<tr> <td>C</td> <td>2</td>  <td>POL</td>          <td>M</td> <td>load port</td>              <td>CNTAG</td> <td>UN/LOCODE - COPRAR 1.2 restricted to same load port, BAPLIE 2.1 allows different load ports</td> </tr>
<!-- D  3 POD -->
<tr> <td>D</td> <td>3</td>  <td>POD</td>          <td>M</td> <td>discharge port</td>         <td>USLAX</td> <td>UN/LOCODE</td> </tr>
<!-- E  4 SIZE_FOOT -->
<tr> <td>E</td> <td>4</td>  <td>STIF_CODE</td>    <td>D</td> <td></td>          <td>40TW</td><td>STIF size and type are used to construct ISO_CODE</td> </tr>
<!-- F  5 ISO_CODE -->
<tr> <td>F</td> <td>5</td> <td>ISO_CODE</td>      <td>D</td> <td>STRING ISO code</td>        <td>22G0</td> <td>overrides STIF_CODE</td> </tr>
<!-- G  6 WEIGHT_KG -->
<tr> <td>G</td> <td>6</td>  <td>WEIGHT_KG</td>    <td>M</td> <td>INT</td>                    <td>22900</td> <td>Gross weight of container and content in kg</td> </tr>
<!-- H  7 EMPTY -->
<tr> <td>H</td> <td>7</td>  <td>EMPTY</td>        <td>O</td> <td>(Y|N)</td>                  <td>Y</td> <td>default N</td> </tr>
<!-- I  8 REEF_LIVE -->
<tr> <td>I</td> <td>8</td>  <td>REEF_LIVE</td>   <td>O</td> <td>(Y|N)</td>                  <td>N</td> <td>default N, only for 20" RF and  40" HR</td> </tr>
<!-- J 9 REEF_TEMP -->
<tr> <td>J</td> <td>9</td> <td>REEF_TEMP</td>    <td>O</td> <td>DOUBLE</td>                 <td>-05.0</td> <td>void for all other containers than RF and HR</td> </tr>
<!-- K 10 TEMP_UNIT -->
<tr> <td>K</td> <td>10</td> <td>TEMP_UNIT</td>    <td>O</td> <td>(CEL|FAH)</td>              <td>CEL</td> <td>only Celsius or Fahrenheit allowed, void for all other containers than RF and HR </td> </tr>
<!-- L 11 IMO_DG -->
<tr> <td>L</td> <td>11</td>  <td>IMO_DG</td>      <td>O</td> <td>STRING</td>                 <td>0335 3268</td> <td>space separated list of UNDG numbers, Angelstow limited quantities format 'ddddL' not COPRAR 1.2 conform</td> </tr>
<!-- M 12 OOG_HEIGHT -->
<tr> <td>M</td> <td>12</td> <td>OOG_HEIGH</td>    <td>O</td> <td>out-of-gauge height</td>          <td>25</td> <td>OOG height in cm</td> </tr>
<!-- N 13 OOG_RIGHT -->
<tr> <td>N</td> <td>13</td> <td>OOG_RIGHT</td>    <td>O</td> <td>out-of-gauge width</td>          <td>12</td> <td>OOG width right in cm</td> </tr>
<!-- O 14 OOG_LEFT -->
<tr> <td>O</td> <td>14</td> <td>OOG_LEFT</td>     <td>O</td> <td>out-of-gauge width</td>          <td>8</td> <td>OOG width left in cm</td> </tr>
<!-- P 15 SPECIAL_STOW -->
<tr> <td>P</td> <td>15</td> <td>SPECIAL_STOW</td> <td>O</td> <td>multiple stow codes</td> <td>AB AL TS AFH DRY INB</td> <td>Two-or-three character Australian Chambers Of Shipping codes</td> </tr>
<!-- Q 16 BOOKING_NO -->
<tr> <td>Q</td> <td>16</td>  <td>BOOKING_NO</td>  <td>O</td> <td>STRING</td>                 <td>B03754</td> <td>any text possible, leave empty for projected load lists</td> </tr>
<!-- R 17 SLOT_POSITION -->
<tr> <td>R</td> <td>17</td>  <td>SLOT_POSITION</td>  <td>D</td> <td>Number 7-digit format BBBRRTT</td>                 <td>0250102</td> <td>BAPLIE and COARRI conversion only, ignored in COPRAR conversion</td> </tr>
<!-- S 18 CARRIER -->
<tr> <td>S</td> <td>18</td>  <td>CARRIER</td>  <td>O</td> <td>STRING 3-char format ABC</td>                 <td>UAS</td> <td>Container carrier code</td> </tr>
</table>

<p>
Commonly used ISO container types including STIF codes are:
</p>

<table class="doc">
<tr> <th>ISO_CODE</th> <th>SIZE</th> <th>TYPE</th> <th>DESCRIPTION</th> <th>STIF_CODE</th> </tr>
<tr> <td>22G0</td> <td>20' x 8'6</td> <td>GP</td> <td>GENERAL PURPOSE</td> <td>20DV</td> </tr>
<tr> <td>22P1</td> <td>20' x 8'6</td> <td>PF</td> <td>PLATFORM (FIXED ENDS)</td> <td>20FR</td> </tr>
<tr> <td>22R0</td> <td>20' x 8'6</td> <td>RE</td> <td>REFRIGERATED</td> <td>20RF</td> </tr>
<tr> <td>22T0</td> <td>20' x 8'6</td> <td>TN</td> <td>TANK (LIQUID)</td> <td>20TK</td> </tr>
<tr> <td>22U1</td> <td>20' x 8'6</td> <td>UT</td> <td>OPEN TOP</td> <td>20OT</td> </tr>
<tr> <td>25G0</td> <td>20' x 9'6</td> <td>GP</td> <td>GENERAL PURPOSE</td> <td>20HC</td> </tr>
<tr> <td>28G0</td> <td>20' x 4'3</td> <td>GP</td> <td>GENERAL PURPOSE</td> <td>20DV</td> </tr>
<tr> <td>29P0</td> <td>20' x <4'</td> <td>PL</td> <td>PLATFORM (PLAIN)</td> <td>20PL</td> </tr>
<tr> <td>42G0</td> <td>40' x 8'6</td> <td>GP</td> <td>GENERAL PURPOSE</td> <td>40DV</td> </tr>
<tr> <td>42P1</td> <td>40' x 8'6</td> <td>PF</td> <td>PLATFORM (FIXED ENDS)</td> <td>40FR</td> </tr>
<tr> <td>42R0</td> <td>40' x 8'6</td> <td>RE</td> <td>REFRIGERATED</td> <td>40RF</td> </tr>
<tr> <td>42T0</td> <td>40' x 8'6</td> <td>TN</td> <td>TANK (LIQUID)</td> <td>40TK</td> </tr>
<tr> <td>42U1</td> <td>40' x 8'6</td> <td>UT</td> <td>OPEN TOP</td> <td>40OT</td> </tr>
<tr> <td>45G0</td> <td>40' x 9'6</td> <td>GP</td> <td>GENERAL PURPOSE</td> <td>40HC</td> </tr>
<tr> <td>45P1</td> <td>40' x 9'6</td> <td>PF</td> <td>PLATFORM (FIXED ENDS)</td> <td>40SR</td> </tr>
<tr> <td>45R0</td> <td>40' x 9'6</td> <td>RE</td> <td>REFRIGERATED</td> <td>40RH</td> </tr>
<tr> <td>49P0</td> <td>40' x <4'</td> <td>PL</td> <td>PLATFORM (PLAIN)</td> <td>40PL</td> </tr>
<tr> <td>L5G0</td> <td>45' x 9'6</td> <td>GP</td> <td>GENERAL PURPOSE</td> <td>45HC</td> </tr>
<tr> <td>L5R0</td> <td>45' x 9'6</td> <td>RE</td> <td>REFRIGERATED</td> <td>45RH</td> </tr>
</table>
