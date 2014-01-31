<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

  <g:render template="/shared/html_head" />

  <body>

    <g:render template="/shared/head" />

    <div id="main" class="main">
      <h2><a href="/convert/excelBaplie/">Convert Excel On-board List
      with Slot Positions to a BAPLIE</a></h2>
      <p>The BAPLIE message is used to transmit complete bayplan
        information about all
        occupied slot positions onboard of a vessel. The BAPLIE is
        exchanged twice between a vessel operator
	and a terminal operator in the next port of call, once as a status
        message before the call, and once after the call operations
        have been performed.
      </p>
      <h2><a href="/convert/excelCoarri/">Convert Excel Load List with
      Slot Positions to a COARRI</a></h2>
      <p>The COARRI (Container discharge/loading report) message is
	used to transmit a confirmation that containers have been
	loaded onto or have been discharged from a seagoing
	vessel, or have been shifted or re-stowed.
        This message is commonly sent by the terminal
	operator handling the vessel to the shipping line (or it’s
	agent). Currently only loaded or shifted/restowed containers
	are implemented.
      </p>
      <h2><a href="/convert/coprar/">Convert Excel Detailed or
      Projected Load List to a COPRAR</a></h2>
      <p>The COPRAR message is send from vessel operator to terminal
	operator to  order loading of containers on, or discharguing
	containers from a seagoing vessel. In addition to containers
	to be loaded or discharged, the COPRAR message may also be
	used to transmit details on containers that are to be shifted
	on board the vessel or containers that are to be discharged
	and reloaded (restowed) onto the vessel. 
      </p>
      <h2><a href="/convert/vesselParser/">Convert Excel Vessel
      Profile to Angelstow Json</a></h2>
      <p></p>
    </div>

    <!--
    <div id="controls">
      <h2>Available Controllers:</h2>
      <ul>
	<g:each var="c" in="${grailsApplication.controllerClasses}">
	<li class="controller"><g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link></li>
	</g:each>
      </ul>
    </div>
    -->

    <g:render template="/shared/foot" />

  </body>
</html>
