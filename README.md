- Link al repositorio:https://github.com/Oyupa/Novelas_Ubicadas.git
- Trabajo realizado por: Adrián Puyo Olías

## Tareas:

Lo he hecho todo en la misma clase así que pondré la refencia a las funciones concretas.

### 1.   Mostrar Ubicación Actual:

#### La aplicación debe permitir a los usuarios ver su ubicación actual en un mapa.
- Lo he conseguido meter en la funciión `setupMap()` de manera que al iniciar la aplicación actualice la ubicación.
  
### 2.      Agregar Ubicación a las Novelas:

#### Permitir a los usuarios asociar una ubicación geográfica con cada novela. Esto puede ser útil para recordar dónde compraron o leyeron la novela.
- Para esto he usado la función longPresHelper() propia de osmdroid (la libreria de OpenStreetMap). Una vez llamada la función, esta, llama a showAddNovelDialog() para mostrar uan ventana en la cual añadir los datos de la novela. Para usar esta funcionalidad presionar en un lugar del mapa hasta que salga el diálogo para añadir la novela; añadir los datos y confirmar.

### 3.      Mostrar Novelas en un Mapa:

#### Visualizar todas las novelas en un mapa, utilizando marcadores para indicar las ubicaciones asociadas.
- A traves de la función loadNovelsFromFirebase(), reciclada de una práctica anterior y modificada, hago que cada vez que se inicie la actividad y cada vez que se añada un marcador con una nueva novela, todas las novelas se descarguen del firebase y se muestren en el mapa usando las coordenadas almacendas.

### 4.      Geocodificación y Geocodificación Inversa:

#### Implementar la funcionalidad para convertir direcciones en coordenadas de latitud/longitud (geocodificación) y viceversa (geocodificación inversa).
- Las coordenadas son tomadas de las novelas e introducidas en los marcadores para adquirir su posicionamiento
### 5.      Interfaz de Usuario:

#### La interfaz debe ser intuitiva y fácil de usar. Utilizar vistas y layouts apropiados para organizar los elementos de la interfaz de usuario.
- La interfaz de usuario es muy intuitiva ya que solamente requiere iniciar la actividad y clicar donde se quiera añadir una novela Además, para navegar por un mapa se hace igual que en cualquier aplicación de mapas. Ahora mismo, para comprovar la viabilidad de la aplicación hay 4 novelas añadidas: uan en San francisco, USA y otras 3 en Villanueva de la Cañada, España.

  
