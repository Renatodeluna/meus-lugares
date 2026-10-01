# Meus Lugares

Aplicativo Android para montar um catálogo pessoal de lugares favoritos —
restaurantes, cafeterias, pontos turísticos e lojas — com nota, observações,
foto e localização.

> **Projeto acadêmico** desenvolvido para a disciplina de Desenvolvimento
> Mobile, com foco em Kotlin, Jetpack Compose e arquitetura MVVM.

## Finalidade

Guardar, num só lugar e sem depender de internet, os lugares que você gostou
de visitar: o que achou (nota de 1 a 5), por quê (observações), como era
(foto) e onde fica (latitude/longitude). Tudo fica salvo apenas no aparelho.

Do ponto de vista acadêmico, o projeto exercita:

- interface 100% declarativa com Jetpack Compose e Material 3;
- navegação entre telas com argumentos tipados;
- arquitetura MVVM com `ViewModel`, `StateFlow` e Repository;
- persistência local com DataStore e serialização JSON;
- permissões em tempo de execução, câmera e geolocalização;
- testes unitários de persistência e de ViewModels.

## Funcionalidades

- **Cadastrar, listar, ver detalhes, editar e excluir** lugares (CRUD completo).
- **Categoria** (restaurante, cafeteria, ponto turístico, loja) e **nota** de 1 a 5 estrelas.
- **Foto** tirada pela câmera, exibida no formulário e nos detalhes.
- **Localização atual** pelo GPS, preenchendo latitude e longitude.
- **Validação** do formulário (nome obrigatório, com erro no próprio campo).
- **Confirmação** antes de excluir e **mensagens de feedback** (Snackbar) ao salvar e excluir.
- **Tema claro e escuro**, com contraste de cores revisado (WCAG AA).

Câmera e localização são **opcionais**: se o usuário negar as permissões, o app
explica o motivo e continua funcionando normalmente, com esses campos em branco.

## Fluxo de uso

```
┌──────────────┐  toque no card   ┌──────────────┐   Editar   ┌──────────────────┐
│    Lista     │ ───────────────► │   Detalhes   │ ─────────► │ Cadastro/Edição  │
│ (tela inicial│                  │ foto, nota,  │            │ nome, categoria, │
│  com cards)  │ ◄─────────────── │ local, data  │ ◄───────── │ nota, foto, GPS  │
└──────┬───────┘     Excluir      └──────────────┘   Salvar   └──────────────────┘
       │ botão +                                                        ▲
       └────────────────────────────────────────────────────────────────┘
```

1. **Lista** — tela inicial. Mostra os lugares em cards (nome, categoria,
   estrelas e trecho da observação), do mais recente para o mais antigo. Sem
   lugares cadastrados, exibe uma mensagem convidando a adicionar o primeiro.
2. **Cadastro** — pelo botão **+**. Preencha nome, categoria, nota e
   observações; opcionalmente **Tirar foto** e **Usar minha localização
   atual**. **Salvar** volta para a lista com a mensagem "Lugar salvo com
   sucesso".
3. **Detalhes** — ao tocar num card. Mostra todos os dados, incluindo foto,
   coordenadas e data de cadastro.
4. **Edição** — pelo botão **Editar** nos detalhes. Mesma tela do cadastro,
   já preenchida.
5. **Exclusão** — pelo botão **Excluir** nos detalhes, com confirmação. Volta
   para a lista com a mensagem "Lugar excluído".

## Tecnologias

| Área | Tecnologia |
|---|---|
| Linguagem | Kotlin 2.4 |
| Interface | Jetpack Compose + Material 3 (sem layouts XML) |
| Navegação | Navigation Compose com rotas type-safe |
| Arquitetura | MVVM (`ViewModel` + `StateFlow`) + Repository, injeção de dependência manual |
| Persistência | Preferences DataStore + kotlinx.serialization (JSON) |
| Imagens | Coil 3 |
| Câmera | `ActivityResultContracts.TakePicture` + `FileProvider` |
| Localização | Google Play Services Location (`FusedLocationProviderClient`) |
| Testes | JUnit 4 + kotlinx-coroutines-test |
| Build | Gradle 9.5, Android Gradle Plugin 9.3, version catalog |

**Android:** `minSdk 26` (Android 8.0) · `targetSdk`/`compileSdk 37`.

## Estrutura do projeto

```
app/src/main/java/br/com/renatodeluna/meuslugares/
├── MainActivity.kt              # única Activity, hospeda a navegação
├── MeusLugaresApplication.kt    # cria o container de dependências
├── di/                          # AppContainer (injeção de dependência manual)
├── domain/model/                # Place, PlaceCategory, Coordinates
├── data/
│   ├── local/                   # DataStore, serialização JSON, arquivos de foto
│   ├── location/                # acesso ao GPS
│   └── repository/              # PlacesRepository
└── ui/
    ├── navigation/              # rotas e NavHost
    ├── components/              # componentes reutilizáveis (card, estrelas, foto...)
    ├── screens/
    │   ├── list/                # tela de lista
    │   ├── form/                # tela de cadastro/edição
    │   └── detail/              # tela de detalhes
    └── theme/                   # cores, tipografia e tema Material 3
```

Cada tela tem um `Screen` (liga o ViewModel à interface), um `ViewModel`
(regras e estado) e um `UiState` (dados exibidos).

## Como instalar e rodar

### Pré-requisitos

- [Android Studio](https://developer.android.com/studio) em versão estável
  recente (com suporte ao Android Gradle Plugin 9.3).
- Android SDK Platform 37 (o Android Studio oferece instalar ao abrir o projeto).
- JDK 21 — o Android Studio já traz um (JetBrains Runtime).
- Um emulador ou aparelho com Android 8.0 ou superior. Para testar a
  localização, use uma imagem de emulador **com Google Play** (ou um aparelho
  com Google Play Services).

### Pelo Android Studio

1. Clone o repositório:
   ```bash
   git clone https://github.com/Renatodeluna/meus-lugares.git
   ```
2. No Android Studio, **File → Open** e selecione a pasta `meus-lugares`.
3. Aguarde a sincronização do Gradle (na primeira vez ele baixa as dependências).
4. Escolha um emulador ou aparelho e clique em **Run ▶**.

### Pela linha de comando

Com o `JAVA_HOME` apontando para um JDK 21 (por exemplo, o do Android Studio)
e o SDK configurado (`ANDROID_HOME` ou `local.properties` com `sdk.dir`):

```bash
# Linux/macOS
./gradlew assembleDebug        # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # instala no emulador/aparelho conectado
./gradlew testDebugUnitTest    # roda os testes unitários
```

```powershell
# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
.\gradlew.bat testDebugUnitTest
```

### Dicas para testar no emulador

- **Câmera:** o emulador usa uma câmera virtual; a foto tirada é uma cena
  simulada.
- **Localização:** a posição padrão do emulador é a sede do Google, na
  Califórnia. Para mudar, abra **Extended Controls (⋯) → Location**, escolha
  um ponto no mapa e clique em **Set location**.
- **Permissões:** para testar de novo o pedido de permissão, revogue em
  **Configurações → Apps → Meus Lugares → Permissões**.

## Permissões

| Permissão | Para quê | Se negada |
|---|---|---|
| `CAMERA` | Tirar a foto do lugar | O lugar é salvo sem foto |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Preencher latitude e longitude | Os campos de localização ficam em branco |

As permissões só são pedidas quando o usuário toca no botão correspondente.

## Testes

Os testes unitários rodam na JVM, sem emulador, e cobrem:

- serialização JSON (inclusive dados corrompidos, que viram lista vazia);
- repositório (inserir, editar, excluir e escritas concorrentes);
- armazenamento das fotos (mover do cache para o armazenamento interno, apagar);
- ViewModels das três telas (validação, edição, exclusão, foto e localização).

```bash
./gradlew testDebugUnitTest
```

## Armazenamento dos dados

- A lista de lugares fica num Preferences DataStore, serializada em JSON.
- As fotos ficam no armazenamento interno do app. Ao excluir um lugar ou
  trocar a foto, o arquivo antigo é apagado.
- Nenhum dado sai do aparelho.
