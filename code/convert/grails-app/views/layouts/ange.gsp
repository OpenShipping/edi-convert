<html>
    <head>
        <title><g:layoutTitle default="Ange - stowbase" /></title>
        <link rel="stylesheet" href="${resource(dir:'css',file:'ange.css')}" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:layoutHead />
        <g:javascript library="application" />
    </head>
    <body>
        <div id="spinner" class="spinner" style="display:none;">
            <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
        </div>
        <div id="angeLogo" class="logo">
            <a href="http://ange.dk"><img src="${resource(dir:'images',file:'ange_logo.png')}" alt="}}" border="0" /></a>
        </div>
        <hr />
        <g:layoutBody />
    </body>
</html>
