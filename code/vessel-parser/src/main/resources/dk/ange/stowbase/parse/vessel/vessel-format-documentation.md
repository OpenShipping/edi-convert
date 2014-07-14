Documentation of the vessel format in XLS files
===============================================

This is still a work in progress, must of the documentation is missing as it is right now.

Sheet: Vessel
-------------

### Name
The name of the vessel

### IMO Number
The IMO number of the vessel

### Longitudinal positive direction
The positive direction of coordinates in the longitudinal direction, the value can be _FORE_ (this is default) or _AFT_.

### Transverse positive direction
The positive direction of coordinates in the transverse direction, the value can be:

_PORT_
:   All transverse positions in the XLS sheet is read as the positive direction is to the port

_STARBOARD_
:   All transverse positions in the XLS sheet is read as the positive direction is to the starboard

_LEGACY_ (default)
:   Most transverse positions in the XLS sheet is read as the positive direction is to the port, but the tanks are
    read with positive direction to the starboard. This is *not* a format we suggest you use, it exists in order to
    provide compatibility with old XLS profile files.


Sheet: Bays
-----------


Sheet: Tier20
-------------


Sheet: Tier40
-------------


Sheet: Slots45
--------------


Sheet: Reef20
-------------


Sheet: Reef40
-------------


Sheet: Wgt20
------------


Sheet: Wgt40
------------


Sheet: Height20
---------------


Sheet: Height40
---------------


Sheet: Pos20
------------
Tables of LCG and TCG of stack bottoms for 20' positions.


Sheet: Pos40
------------
Tables of LCG and TCG of stack bottoms for 40' positions.


Sheet: Lids
-----------


Sheet: DG
---------


Sheet: Tanks
------------

Table over all the tanks with the following columns.

Description
:   Name of tank

Tank Group
:   Tank group, e.g. "Ballast"

Capacity in m3
:   Volume capacity in cubic meters, this column is optional and is only used to give a warning if the numbers
    doesn't match with the mass capacity numbers in the next column

Capacity in ton
:   Mass capacity in metric ton

Density in ton/m3
:   Density used to convert between mass and volume

Fore End in m
:   For end of tank, is used for spanning tanks

Aft End in m
:   Aft end of tank, is used for spanning tanks

LCG in m
:   LCG of full tank, if overridden by value from VarTanks sheet the number will be checked for consistency

VCG in m
:   VCG of full tank, if overridden by value from VarTanks sheet the number will be checked for consistency

TCG in m
:   TCG of full tank, if overridden by value from VarTanks sheet the number will be checked for consistency

Max FSM in m4
:   Max free surface moment of tank, if overridden by value from VarTanks sheet the number will be checked for
    consistency


Sheet: VarTanks
---------------

Blocks of data with the following fields:

Description
:   Name of tank, used to find which tank in the Tanks sheet this block is a part of

Volume in m3
:   Volume this measurement is for

LCG in m
:   LCG of given volume

VCG in m
:   VCG of given volume

TCG in m
:   TCG of given volume

Max FSM in m4
:   Free surface moment of tank for given volume.
    "FSM in m4" would have been a better name for this field.


Sheet: Stability
----------------


Sheet: ConstWgts
----------------

Column A
:   Description, name of weight

Column B
:   Aft LCG in m

Column C
:   Fore LCG in m

Column D
:   Aft density in ton/m

Column E
:   Fore density in ton/m

Column F
:   TCG in m

Column G
:   VCG in m


Sheet: HullWgtDistr
-------------------


Sheet: Bonjean
--------------


Sheet: Hydrostatics
-------------------


Sheet: MetaCenter
-----------------


Sheet: StressLimits
-------------------

