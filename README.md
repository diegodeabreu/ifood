# 🍳 IFood Processo - Spoonacular Recipe App

Aplicativo móvel de nível *Top-Tier / Enterprise* desenvolvido em **Kotlin Multiplatform (KMP)** e **Jetpack / Compose Multiplatform**, projetado para consultar receitas culinárias utilizando a API da **Spoonacular**.

---

## 🏛️ Arquitetura & Padrões
O projeto segue estritamente o padrão **MVVM (Model-View-ViewModel)** combinado com princípios de **Clean Architecture**, dividindo as responsabilidades em camadas bem definidas no módulo compartilhado (`:shared`):

* **Data Layer**: Clientes de rede (`Ktor` com `Kotlinx Serialization`), DTOs, Repositórios e Banco de Dados relacional local (`SQLDelight`).
* **Domain Layer**: Modelos de domínio puros, Repositórios (interfaces), *Use Cases* encapsulando as regras de negócio e hierarquia tipada de erros (`AppError` / `DomainResult`).
* **Presentation Layer**: ViewModels reativos (`StateFlow` / `UIState`) com suporte a restauração de estado pós-morte de processo (`SavedStateHandle`) e telas em **Jetpack Compose / Compose Multiplatform**.

---

## 🛠️ Stack Tecnológica & Padrões Avançados
* **Kotlin Multiplatform (KMP)** & **Compose Multiplatform** (UI declarativa compartilhada).
* **Ktor Client** & **Kotlinx Serialization** (Comunicação HTTP assíncrona).
* **SQLDelight** (Banco de dados SQLite multiplataforma para persistência offline e favoritos).
* **Koin** (Injeção de dependências em KMP, validada em tempo de teste via `checkKoinModules`).
* **Coil 3** (Carregamento assíncrono de imagens com suporte a efeito *Shimmer*).
* **Turbine & Coroutines Test** (Testes avançados de fluxos reativos `Flow`).
* **Roborazzi & Robolectric** (Testes de snapshot e regressão visual de UI).
* **Detekt (`detekt.yml`) & KtLint** (Análise estática rigorosa e formatação automática).
* **R8 / ProGuard** (Ofuscação e encolhimento de código para builds de release com regras protetivas).
* **Product Flavors (`staging` & `production`)** (Isolamento de ambientes e IDs de aplicação).
* **GitHub Actions CI/CD** (Pipeline automatizado de qualidade e builds).

---

## ✨ Principais Funcionalidades
1. **Busca Inteligente em PT-BR**: Suporte a consultas em português com dicionário de tradução inteligente (ex: *"frango"* ➔ *"chicken"*).
2. **Paginação com Auto-Scroll**: Navegação paginada (*« Prev / Page X of Y / Next »*) com reposicionamento automático para o topo da lista.
3. **Detalhes Completos & Recomendações**: Ingredientes, modo de preparo, tempo de preparo, porções e recomendações de receitas semelhantes clicáveis.
4. **Favoritos Persistentes e Cache Offline**: Salvamento automático em banco de dados SQLite local (`SQLDelight`) e fallback resiliente sem internet.
5. **Compartilhamento Nativo (`Share Recipe`)**: Exportação formatada dos detalhes da receita via *Share Intent* usando expect/actual.
6. **Confirmação de Saída (Duplo Voltar)**: Ao pressionar o botão voltar na tela inicial, o app exige um segundo toque para sair, exibindo um aviso via `Snackbar`.
7. **Tratamento Global de Erros**: Notificações automáticas via `Snackbar` baseadas em um canal reativo bufferizado (`ErrorNotifier`).
8. **Analytics Telemetry Abstrato**: Camada pronta para registrar eventos de telemetria e visualizações de tela.

---

## 🔑 Segurança da Chave de API
Para proteger a chave de API da Spoonacular contra exposição pública, utilizamos a estratégia de **`local.properties` + `BuildConfig` + Koin**:
1. Adicione a chave no arquivo `local.properties` (excluído do Git):
   ```properties
   spoonacular.api.key=SUA_CHAVE_AQUI
   ```
2. A chave é injetada no build via `BuildConfig` e repassada de forma segura para o cliente de rede através da injeção de dependências do Koin.

---

## 🚀 Como Executar o Projeto

* **Android App (Debug - Staging)**:
  `./gradlew :androidApp:assembleStagingDebug`
* **Gerar Builds por Ambiente (Ofuscadas com R8)**:
  - Staging Release: `./gradlew :androidApp:assembleStagingRelease`
  - Production Release: `./gradlew :androidApp:assembleProductionRelease`

---

## 🧪 Testes & Qualidade de Código

* **Testes Unitários, Fluxos (Turbine) & DI Graph (`checkKoinModules`)**:
  `./gradlew :androidApp:testStagingDebugUnitTest`
* **Testes de Snapshot (Roborazzi)**:
  `./gradlew :androidApp:testStagingDebugUnitTest` (ou gravação via `./gradlew recordRoborazziDebug`)
* **Auditoria Completa de Qualidade (Detekt & KtLint)**:
  `./gradlew checkAllQuality`
* **Formatação Automática e Limpeza de Imports**:
  `./gradlew ktlintFormat`
