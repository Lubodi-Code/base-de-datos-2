# 🗄️ Base de Datos 2

Proyecto colaborativo para la materia **Base de Datos 2**. Aquí trabajamos en equipo con esquemas, consultas SQL, procedimientos almacenados, triggers, vistas y más.

---

## 📁 Estructura del Proyecto

```
base-de-datos-2/
├── esquemas/           # Scripts DDL (CREATE TABLE, ALTER, etc.)
├── consultas/          # Consultas SQL (SELECT, JOIN, subconsultas)
├── procedimientos/     # Stored Procedures y Funciones
├── triggers/           # Triggers de la BD
├── vistas/             # Vistas (CREATE VIEW)
├── datos/              # Scripts de inserción de datos de prueba
├── reportes/           # Reportes y documentación del proyecto
└── README.md
```

---

## 🚀 Cómo Contribuir

1. **Clona el repositorio**
2.    ```bash
         git clone https://github.com/Lubodi-Code/base-de-datos-2.git
         cd base-de-datos-2
         ```

      2. **Crea una rama para tu tarea**
      3.    ```bash
               git checkout -b feature/nombre-de-tu-tarea
               ```

            3. **Haz tus cambios y commitsea**
            4.    ```bash
                     git add .
                     git commit -m "feat: descripción de lo que hiciste"
                     ```

                  4. **Sube tu rama y abre un Pull Request**
                  5.    ```bash
                           git push origin feature/nombre-de-tu-tarea
                           ```

                        ---

                    ## 📝 Convención de Commits

              Usamos el estilo **Conventional Commits**:

        | Tipo | Uso |
  |------|-----|
  | `feat:` | Nueva funcionalidad o script |
  | `fix:` | Corrección de errores en consultas/scripts |
  | `docs:` | Cambios en documentación |
  | `refactor:` | Mejora de scripts sin cambiar funcionalidad |
  | `data:` | Inserción o modificación de datos de prueba |

  ---

  ## 👥 Equipo

  | Nombre | Usuario GitHub | Rol |
  |--------|---------------|-----|
  | (Agrega tu nombre) | @usuario | Desarrollador |

  ---

  ## ⚙️ Motor de Base de Datos

  - **DBMS:** (ej. MySQL 8.0 / PostgreSQL 15 / SQL Server)
  - - **Nombre de la BD:** (nombre del esquema)
   
    - ---

    ## 📌 Reglas del Proyecto

    - Siempre trabajar en una rama propia, **nunca directo en `main`**.
    - - Todo cambio entra por **Pull Request** con al menos 1 revisión.
      - - Los scripts deben ser **idempotentes** (usar `IF NOT EXISTS`, `DROP IF EXISTS`).
        - - Documentar cada script con comentarios explicando su propósito.
         
          - ---

          ## 📚 Recursos Útiles

          - [Documentación MySQL](https://dev.mysql.com/doc/)
          - - [Documentación PostgreSQL](https://www.postgresql.org/docs/)
            - - [SQL Tutorial - W3Schools](https://www.w3schools.com/sql/)
              - 
