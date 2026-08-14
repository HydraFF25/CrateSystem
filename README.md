# CrateSystem

CrazyCrates / ExcellentCrates tarzı, **CS:GO rulet animasyonlu** özel kasa (crate) açma eklentisi.
Paper/Spigot **1.21.x** için Java 17 ile yazılmıştır.

## Özellikler

- **Fiziksel kasa**: Bir bloğa (örn. sandık, ender chest, her ne blok istersen) sağ tıklayarak kasa açma.
- **GUI menüsü**: `/crate` veya `/crate menu` ile tüm kasaların listelendiği menü.
- **CS:GO tarzı rulet animasyonu**: Ödüller bir şerit üzerinde kayar, yavaşlar ve bir ödülde durur.
- **Fiziksel anahtar itemleri**: Görsel/isim/lore tamamen özelleştirilebilir, PDC ile hangi kasaya ait olduğu işaretlenir.
- **Sanal anahtarlar**: Envanterde yer kaplamayan, `/key vgive` ile verilen anahtarlar.
- **Ağırlıklı (weighted) rastgelelik**: Her ödülün `chance` değeri, o ödülün diğerlerine göre ne kadar sık çıkacağını belirler (yüzde olmak zorunda değil, oransal ağırlıktır).
- **Komut ödülleri**: Ödül item vermek yerine (veya yerine ek olarak) konsol komutu çalıştırabilirsin (`eco give %player% 1000` gibi — ekonomi eklentin varsa).
- Her kasa ayrı bir `.yml` dosyasında tanımlanır → sınırsız kasa türü ekleyebilirsin.

## Kurulum

1. Bu klasörü bir sunucuda (internet erişimi olan bir makinede) derle:
   ```
   mvn clean package
   ```
   Bu adım `paper-api` bağımlılığını internetten indirir, bu yüzden derleme sırasında internet gereklidir (sunucuda çalıştırmak için değil, sadece derlerken).
2. `target/CrateSystem.jar` dosyasını sunucunun `plugins/` klasörüne at.
3. Sunucuyu başlat/reload et. Eklenti `plugins/CrateSystem/config.yml` ve `plugins/CrateSystem/crates/example.yml` dosyalarını otomatik oluşturacak.

## Kasa Oluşturma

`plugins/CrateSystem/crates/` klasörüne yeni bir `.yml` dosyası ekle (dosya adı = kasa id'si).
`example.yml` dosyasını kopyalayıp örnek alabilirsin. Alanlar:

- `display-name`: Kasanın görünen adı.
- `key`: Anahtar itemin görünümü (material, name, lore).
- `display-item`: Menüde görünecek item.
- `rewards`: Ödül listesi — her ödülde `material`, `amount`, `name`, `lore`, `chance` (ağırlık), `commands` (opsiyonel).
- `physical-locations`: `/crate setblock` ile otomatik doldurulur, elle de eklenebilir (`dunya,x,y,z` formatında).

Değişiklik yaptıktan sonra `/crate reload` ile yeniden yükle.

## Komutlar

| Komut | Yetki | Açıklama |
|---|---|---|
| `/crate` | `cratesystem.use` | Kasa menüsünü açar |
| `/crate menu` | `cratesystem.use` | Kasa menüsünü açar |
| `/crate open <kasa>` | `cratesystem.use` | İsimle kasa açmayı dener (anahtar gerekir) |
| `/crate list` | - | Tüm kasa id'lerini listeler |
| `/crate give <oyuncu> <kasa> [miktar]` | `cratesystem.admin` | Fiziksel anahtar verir |
| `/crate setblock <kasa>` | `cratesystem.admin` | Baktığın bloğu o kasa için fiziksel kasa yapar |
| `/crate removeblock` | `cratesystem.admin` | Baktığın bloktaki fiziksel kasayı kaldırır |
| `/crate reload` | `cratesystem.admin` | Config ve kasaları yeniden yükler |
| `/key give <oyuncu> <kasa> [miktar]` | `cratesystem.admin` | Fiziksel anahtar verir |
| `/key vgive <oyuncu> <kasa> [miktar]` | `cratesystem.admin` | Sanal (envantersiz) anahtar verir |

## Yetkiler

- `cratesystem.use` (varsayılan: herkes) — kasa açabilme.
- `cratesystem.admin` (varsayılan: op) — anahtar verme, kasa bloğu ayarlama, reload.

## Animasyon Ayarları (`config.yml`)

```yaml
settings:
  animation:
    total-steps: 30       # animasyonun kaç adımdan oluştuğu (süreyi etkiler)
    start-delay-ticks: 1   # ilk adımlar arası gecikme (hızlı)
    max-delay-ticks: 9     # son adımlar arası gecikme (yavaş, "duruş" hissi)
```

## Notlar / Genişletme Fikirleri

- Şu an ödül olarak sadece item + opsiyonel komut destekleniyor; istersen `CrateReward` sınıfına
  ekonomi entegrasyonu (Vault) veya broadcast mesajı gibi ek alanlar kolayca eklenebilir.
- İstersen fiziksel kasaya sağ tıklayınca direkt rulet animasyonu yerine önce bir onay/parçacık
  bekleme adımı da eklenebilir — `BlockInteractListener` içinden kolayca özelleştirilir.
- Sanal + fiziksel anahtar sistemi birlikte çalışır: oyuncunun fiziksel anahtarı öncelikli kullanılır.
