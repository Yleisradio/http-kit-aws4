# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.3.0]
### Fixed:
- Issue #11 fixed by caching AWS credentials in memory for 60 seconds

## [0.2.0]
### Changed:
- Update AWS Maven wagon version
### Fixed:
- Query parameters included in request :url are now included in canonical-request (#10)

## [0.1.2-alpha]
### Changed:
- Updated httpkit to 2.4-alpha3 to support JDK 11
- Version kept at alpha until httpkit changes are released

## [0.1.1]
### Added
- README: Licence
- CHANGELOG

### Changed
- Upgraded outdated dependencies:
```
[buddy "2.0.0"] is available but we use "1.3.0"
[cheshire "5.8.0"] is available but we use "5.7.1"
```

## 0.1.0 - 2017-09-15 
### Added
- Initial release

[Unreleased]: https://github.com/Yleisradio/http-kit-aws4/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/Yleisradio/http-kit-aws4/compare/v0.1.2-alpha...v0.2.0
[0.1.2-alpha]: https://github.com/Yleisradio/http-kit-aws4/compare/v0.1.1...v0.1.2-alpha
[0.1.1]: https://github.com/Yleisradio/http-kit-aws4/compare/v0.1.0...v0.1.1
