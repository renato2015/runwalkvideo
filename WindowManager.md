# Window wrapping #
  1. wrappen gebeurt door de WindowManager die een method heeft met als argument interface 2 => addWindow( ... ) => welke methods hebben we daar nodig?
  1. Definieren van een ActionMap door het mergen van die van de AppInternalFrame (voor de TOGGLE\_VISIBILITY) met die van <interface 2>
  1. In het geval van een dispose() (unwrapping) => component onzichtbaar maken => hoe nieuwe actie terugkoppelen?

# Visibility toggling #
ENKEL de verantwoordelijkheid van self contained windows, concreet door ofwel implementeren van een ComponentListener of een andere manier van events opvangen

  1. AppInternalFrame
  1. DSJComponent (in het geval die fullscreen is)
  1. UEyeCapturer (hoe de events hier vastkrijgen?)

# Fullscreen toggling #
verantwoordelijkheid van de implementatie zelf

  1. self contained: gewoon setFullScreen aanroepen en verder niks meer van aantrekken!
  1. niet self contained: niet implementeren
  1. self EN niet self contained (bvb DSjComponent):

  * moet gewrapped worden als van self contained naar niet self contained (eerst call naar setFullscreen, dan via bestaande logica window wrappen en tonen)

  * moet unwrapped worden als van niet self contained naar self container (gewoon component vrijzetten, kijk oude showComponent() code hiervoor, dan call naar setFullscreen)

  * wrapping/unwrapping op het niveau van WindowManager

# Interface Description #
  1. AppWindowWrapper
isVisible()
setVisible()
toggleVisibility()
toFront()
getTitle() - zodat de menu en mediacontrols de juiste titel kunnen weergeven
  1. TBD